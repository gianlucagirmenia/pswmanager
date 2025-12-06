package com.durdencorp.pswmanager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
 info = @Info(
     title = "Password Manager API",
     version = "1.0.0",
     description = """
         Single-user password management system secured by master password.
         
         **Features:**
         - Secure password storage with master password protection
         - Password generation tools
         - Password strength checking
         - No multi-user authentication required
         """,
     contact = @Contact(
         name = "Gianluca Girmenia",
         email = "gianlucagirmenia@gmail.com",
         url = "https://github.com/gianlucagirmenia/pswmanager"
     ),
     license = @License(
         name = "MIT License",
         url = "https://opensource.org/licenses/MIT"
     )
 ),
 servers = {
     @Server(
         description = "Local Development Server",
         url = "http://localhost:6969"
     )
 }
)
public class OpenApiConfig {
}