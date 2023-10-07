package com.company.web.springdemo.repositories;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.models.Style;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@PropertySource("classpath:application.properties")
public class StyleRepositorySQLImpl implements StyleRepository{

    private final String dbUrl, dbUsername, dbPassword;

    public StyleRepositorySQLImpl(Environment env) {
        dbUrl = env.getProperty("database.url");
        dbUsername = env.getProperty("database.username");
        dbPassword = env.getProperty("database.password");
    }

    @Override
    public List<Style> get() {
        String query = "select * from style";

        try(
                Connection connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                ){
            List<Style> result = getStyles(resultSet);
            return result;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Style get(int id) {
        String query = "select id, style_name " +
                "from style " +
                "where id = ?";

        try(
                Connection connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setInt(1,id);
            try(
                    ResultSet resultSet = preparedStatement.executeQuery();
            ){
                List<Style> result = getStyles(resultSet);
                if(result.isEmpty()){
                    throw new EntityNotFoundException("Style",id);
                }
                return result.get(0);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    private List<Style> getStyles(ResultSet stylesData) throws SQLException{
        List<Style> result = new ArrayList<>();
        while(stylesData.next()){
            Style style = new Style(
                    stylesData.getInt("id"),
                    stylesData.getString("style_name"));
            result.add(style);
        }
        return result;
    }
}
