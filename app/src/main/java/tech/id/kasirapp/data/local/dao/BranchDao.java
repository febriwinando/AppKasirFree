package tech.id.kasirapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import tech.id.kasirapp.data.local.entity.Branch;
@Dao
public interface BranchDao {
    @Insert
    long insert(Branch branch);
}