package tech.id.kasirapp.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import tech.id.kasirapp.data.firebase.FirebaseRepository;
import tech.id.kasirapp.data.local.AppDatabase;
import tech.id.kasirapp.data.local.entity.Branch;
import tech.id.kasirapp.data.local.entity.Manager;
import tech.id.kasirapp.data.local.entity.Menu;
import tech.id.kasirapp.data.local.entity.Restaurant;
import tech.id.kasirapp.data.local.entity.Waiter;

public class DeletionHelper {

    public interface OnDeleteCompleteListener {
        void onSuccess();
        void onFailed(String error);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void deleteRestaurant(Context context, AppDatabase db, Restaurant restaurant, OnDeleteCompleteListener listener) {
        executor.execute(() -> {
            try {
                List<Branch> branches = db.branchDao().getByRestaurant(restaurant.id);
                AtomicInteger remainingBranches = new AtomicInteger(branches.size());

                if (branches.isEmpty()) {
                    deleteRestaurantOnly(db, restaurant, listener);
                    return;
                }

                for (Branch branch : branches) {
                    deleteBranchData(context, db, branch, new OnDeleteCompleteListener() {
                        @Override
                        public void onSuccess() {
                            if (remainingBranches.decrementAndGet() == 0) {
                                deleteRestaurantOnly(db, restaurant, listener);
                            }
                        }

                        @Override
                        public void onFailed(String error) {
                            listener.onFailed(error);
                        }
                    });
                }
            } catch (Exception e) {
                listener.onFailed(e.getMessage());
            }
        });
    }

    public static void deleteBranchData(Context context, AppDatabase db, Branch branch, OnDeleteCompleteListener listener) {
        executor.execute(() -> {
            try {
                FirebaseRepository repo = new FirebaseRepository();

                // 1. Fetch related data
                List<Menu> menus = db.menuDao().getByBranch(branch.id);
                List<Waiter> waiters = db.waiterDao().getByBranchId(branch.id);
                Manager manager = db.managerDao().getByBranchId(branch.id);

                // 2. Delete Menu images and docs
                for (Menu menu : menus) {
                    repo.deleteImage(menu.imagePath, null);
                    repo.deleteMenu(menu.firebaseId, null);
                    deleteLocalFile(menu.imagePath);
                }

                // 3. Delete Waiter images and docs
                for (Waiter waiter : waiters) {
                    repo.deleteImage(waiter.photoPath, null);
                    repo.deleteImage(waiter.ktpPhotoPath, null);
                    repo.deleteWaiter(waiter.firebaseId, null);
                    deleteLocalFile(waiter.photoPath);
                    deleteLocalFile(waiter.ktpPhotoPath);
                }

                // 4. Delete Manager doc
                if (manager != null) {
                    repo.deleteManager(manager.firebaseId, null);
                }

                // 5. Delete Branch doc
                repo.deleteBranch(branch.firebaseId, new FirebaseRepository.OnCompleteListener() {
                    @Override
                    public void success() {
                        // 6. Delete from local Room
                        executor.execute(() -> {
                            db.menuDao().deleteByBranch(branch.id);
                            db.waiterDao().deleteByBranchId(branch.id);
                            db.managerDao().deleteByBranchId(branch.id);
                            db.branchDao().deleteById(branch.id);
                            listener.onSuccess();
                        });
                    }

                    @Override
                    public void failed(String error) {
                        // Even if server delete fails, we might want to clean up local
                        listener.onFailed(error);
                    }
                });

            } catch (Exception e) {
                listener.onFailed(e.getMessage());
            }
        });
    }

    private static void deleteRestaurantOnly(AppDatabase db, Restaurant restaurant, OnDeleteCompleteListener listener) {
        FirebaseRepository repo = new FirebaseRepository();
        repo.deleteRestaurant(restaurant.firebaseId, new FirebaseRepository.OnCompleteListener() {
            @Override
            public void success() {
                executor.execute(() -> {
                    db.restaurantDao().delete(restaurant);
                    listener.onSuccess();
                });
            }

            @Override
            public void failed(String error) {
                listener.onFailed(error);
            }
        });
    }

    private static void deleteLocalFile(String path) {
        if (path != null && !path.startsWith("http")) {
            try {
                File file = new File(Uri.parse(path).getPath());
                if (file.exists()) file.delete();
            } catch (Exception ignored) {}
        }
    }
}