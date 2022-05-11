package dev.westernpine.site.routes;

import spark.Request;
import spark.Response;
import spark.Route;

public class NotFound implements Route {

    public static String toString = """
                <!DOCTYPE html>
                				<html>
                					<body>
                						<h1>Uh oh, I lost the page...</h1>
                						Lets restart using the link below. If the issue persists, please contact the site administrator.
                						<br>
                						<br><a href="/oauth/logout">Country Roads, Take Me Home!</a>
                					</body>
                				</html>
                """;

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.redirect("/");
        return null;
    }
}
