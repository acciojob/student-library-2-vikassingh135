package com.driver.controller;

//Add required annotations

import com.driver.models.Author;
import com.driver.services.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorController {

    @Autowired
    private AuthorService authorService;
    
    //Write createAuthor API with required annotations
    @PostMapping("/author")
    public ResponseEntity<String> createAuthor(@RequestBody Author author) {
        authorService.create(author);
        return new ResponseEntity<>("Success",HttpStatus.OK);
    }
}
