package app.controllers;

import app.entities.Task;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.TaskMapper;
import app.persistence.UserMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserController {

    public static void addRoutes(Javalin app, ConnectionPool connectionPool) {
        app.post("login", ctx -> login(ctx, connectionPool));
        app.get("logout", ctx -> logout(ctx));
        app.get("createuser", ctx -> ctx.render("createuser.html"));
        app.post("createuser", ctx -> createUser(ctx, connectionPool));

    }

    private static void logout(Context ctx) {
        ctx.req().getSession().invalidate();  //sletter alt der ligger på nuværende session af lokale variable i browser
        ctx.redirect("/"); //redirect nulstiller url'en. Render bruges oftest, men redirect kan være nyttig (især hvis man skal henvise til anden hjemmeside!)
    }

    private static void createUser(Context ctx, ConnectionPool connectionPool) {
        String username = ctx.formParam("username");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

        if(password1.equals(password2)) {
            try {
                UserMapper.createuser(username, password1, connectionPool);
                ctx.attribute("message", "Du er hermed oprettet med bruger: " + username +
                        ". Nu skal du logge på!");
                ctx.render("index.html");

            } catch (DatabaseException e) {
                ctx.attribute("message",  "Dit brugernavn findes allerede. Prøve igen, eller log ind!");
                ctx.render("createuser.html");
            }
        } else {
          ctx.attribute("message", "Dine to passwords matcher ikke! Prøve igen!");
          ctx.render("createuser.html");
        }
    }

    public static void login(Context ctx, ConnectionPool connectionPool) {
        //Hent form parametre
        String username = ctx.formParam("username"); // "username" og "password" skal stemme overens med navngivning af attributter i html-fil!
        String password = ctx.formParam("password");



        //Check om bruger findes i DB
        //Hvis ja: Send bruger videre til task-siden
        try {
            User user = UserMapper.login(username, password, connectionPool);
            ctx.sessionAttribute("currentUser", user); //sørger for at man er logget på så længe browserens session er på
            List<Task> taskList = TaskMapper.getAllTasksPerUser(user.getUserId(), connectionPool);
            ctx.attribute("taskList", taskList);
            ctx.render("task.html");
        } catch (DatabaseException e) {
            //Hvis nej: Send tilbage til forside med fejl!
            ctx.attribute("message", e.getMessage());
            ctx.render("index.html");
        }

    }
}
