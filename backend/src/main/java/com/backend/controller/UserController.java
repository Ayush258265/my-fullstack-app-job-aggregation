package com.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.dto.ApiResponse;
import com.backend.entity.User;
import com.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
	@Autowired
	private UserService userService;

	@GetMapping("/hello")
	public ResponseEntity<ApiResponse<String>> hello() {
		return ResponseEntity.ok(
				ApiResponse.success("Backend is working! Security will be added later.", "Hello from Spring Boot!"));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {

		List<User> users = userService.getAllUsers();

		return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));

	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {

		User user = userService.getUserById(id);

		return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));

	}

	public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {

		try {
			User createdUser = userService.createUser(user);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success("User created successfully", createdUser));

		} catch (Exception e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
		
		try {
			User updatedUser = userService.updateUser(id, user);
			
			return ResponseEntity.ok(
					ApiResponse.success("User updated successfully", updatedUser)
			);
			
		}catch(Exception e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())) ;
		}
		
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<User>> deleteUser(@PathVariable Long id) {
		
		try {
			userService.deleteUser(id);
			
			return ResponseEntity.ok(
					ApiResponse.success("User deleted successfully", null)
			);
			
		}catch(Exception e) {
			return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())) ;
		}
		
		
	}
	
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String name)  {
		
		List<User> users = userService.searchUsersByName(name);
		
		return ResponseEntity.ok(
	            ApiResponse.success("Search results retrieved", users)
		);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
