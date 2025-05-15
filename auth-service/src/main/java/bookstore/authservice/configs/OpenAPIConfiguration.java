package bookstore.authservice.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfiguration {
	@Bean
	public OpenAPI defineOpenApi() {
		Server server = new Server();
		server.setUrl("http://localhost:8002");
		server.setDescription("Account Management REST API Documentation");

		Info information = new Info()
				.title("Account Management REST API Documentation")
				.version("1.0")
				.description("This API exposes endpoints to manage accounts.");
		
		return new OpenAPI().info(information).servers(List.of(server));
	}

	@Bean
	public GroupedOpenApi groupedOpenApi() {
		return GroupedOpenApi.builder()
				.group("api_account")
				.pathsToMatch("/api/auth/**")
				.build();
	}
}

//http://localhost:8002/swagger-ui/index.html
//http://localhost:8002/v3/api-docs