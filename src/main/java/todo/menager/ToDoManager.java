package todo.menager;

import todo.db.DBConnectionProvider;
import todo.model.ToDo;
import todo.model.ToDoStatus;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ToDoManager {

    private Connection connection = DBConnectionProvider.getProvider().getConnection();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private UserManager userManager = new UserManager();

    public boolean create(ToDo toDo) {
        String sql = "INSERT INTO todo(title,deadline, status, user_id) VALUES(?,?,?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, toDo.getTitle());
            if (toDo.getDeadline() != null) {
                statement.setString(2, sdf.format(toDo.getDeadline()));
            } else {
                statement.setString(2, null);
            }
            statement.setString(3, toDo.getStatus().name());
            statement.setLong(4, toDo.getUser().getId());
            statement.executeUpdate();

            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                toDo.setId(resultSet.getLong(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(long id, ToDoStatus status) {
        String sql = "UPDATE todo SET status = '" + status.name() + "' WHERE id = " + id;
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM todo WHERE id = " + id;
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ToDo getById(long id) {
        String sql = "SELECT * FROM todo WHERE id = " + id;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return getToDoFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ToDo> getAllToDosByUser(long userId) {
        List<ToDo> toDos = new ArrayList<>();
        String sql = "SELECT * FROM todo WHERE user_id = " + userId;
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                toDos.add(getToDoFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toDos;
    }

    public List<ToDo> getAllToDosByUserAndStatus(long userId, ToDoStatus status) {
        List<ToDo> toDos = new ArrayList<>();
        String sql = "SELECT * FROM todo WHERE user_id = ? AND status = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, userId);
            statement.setString(2, status.name());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                toDos.add(getToDoFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toDos;
    }

    private ToDo getToDoFromResultSet(ResultSet resultSet) {
        try {
            try {
                return ToDo.builder()
                        .id(resultSet.getLong(1))
                        .title(resultSet.getString(2))
                        .deadline(resultSet.getString(3) == null ?
                                null : sdf.parse(resultSet.getString(3)))
                        .status(ToDoStatus.valueOf(resultSet.getString(4)))
                        .user(userManager.getById(resultSet.getLong(5)))
                        .createDate(sdf.parse(resultSet.getString(6)))
                        .build();
            } catch (ParseException e) {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
