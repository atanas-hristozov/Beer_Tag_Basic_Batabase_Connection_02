package com.company.web.springdemo.repositories;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@PropertySource("classpath:application.properties")
public class UserRepositorySQLImpl implements UserRepository{

    private final String dbUrl, dbUsername, dbPassword;

    @Autowired
    public UserRepositorySQLImpl(Environment env) {
        dbUrl = env.getProperty("database.url");
        dbUsername = env.getProperty("database.username");
        dbPassword = env.getProperty("database.password");
    }

    @Override
    public List<User> getAll() {
        String query = "select *" +
                "from users";
        try(
                Connection connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                ){
            List<User> result = getUsers(resultSet);
            return result;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getById(int id) {
        String query = "select id, username " +
                "from users " +
                "where id = ?";

        try(
                Connection connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ){
            preparedStatement.setInt(1,id);
            try(
                    ResultSet resultSet = preparedStatement.executeQuery();
                    ){
                List<User> result = getUsers(resultSet);
                if(result.isEmpty()){
                    throw new EntityNotFoundException("User",id);
                }
                return result.get(0);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getByUsername(String username) {
        String query = "select id, username " +
                "from users " +
                "where username = ?";

        try(
                Connection connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setString(1,username);
            try(
                    ResultSet resultSet = preparedStatement.executeQuery();
            ){
                List<User> result = getUsers(resultSet);
                if(result.isEmpty()){
                    throw new EntityNotFoundException("User","username",username);
                }
                return result.get(0);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

    }

    private List<User> getUsers(ResultSet resultSet) throws SQLException {
        List<User> result = new ArrayList<>();
        while(resultSet.next()){
            User user = new User(
                    resultSet.getInt("id"),
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getBoolean("is_admin"));
            result.add(user);
        }
        return result;
    }
}
