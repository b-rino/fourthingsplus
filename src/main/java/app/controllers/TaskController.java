package app.controllers;

import app.entities.Task;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.TaskMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class TaskController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.post("addtask", ctx -> addtask(ctx, connectionPool));

    }

    private static void addtask(Context ctx, ConnectionPool connectionPool) {

        String taskName = ctx.formParam("taskname"); //Sikr dig at det passer på dit name i task.html!

        if (taskName.length() > 3){
            try {
                User user = ctx.sessionAttribute("currentUser");
                Task newTask = TaskMapper.addTask(user, taskName, connectionPool);
                List<Task> taskList = TaskMapper.getAllTasksPerUser(user.getUserId(), connectionPool);
                ctx.attribute("taskList", taskList);
                ctx.render("task.html");

            } catch (DatabaseException e) {
                ctx.attribute("message", "Noget gik galt. Prøv evt. igen!");
                ctx.render("task.html");
            }
        }
    }
}
