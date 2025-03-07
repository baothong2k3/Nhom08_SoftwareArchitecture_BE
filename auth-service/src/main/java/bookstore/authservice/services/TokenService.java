package bookstore.authservice.services;

import bookstore.authservice.entities.Token;

public interface TokenService {
    void saveToken(Token token);
    Token findByToken(String token);
}
