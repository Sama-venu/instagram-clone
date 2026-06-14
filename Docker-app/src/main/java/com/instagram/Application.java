package com.instagram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.*;
import javax.servlet.http.HttpSession;
import java.util.*;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@Entity
@Table(name = "users")
class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true) private String username;
    @Column(unique = true) private String email;
    private String password;
    private String fullname;
    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
}

@Entity
@Table(name = "posts")
class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String imageUrl;
    private String caption;
    private Date createdAt = new Date();
    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
}

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowCredentials = "true")
class InstagramController {
    @Autowired private UserRepository userRepo;
    @Autowired private PostRepository postRepo;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepo.existsByUsername(user.getUsername()))
            return ResponseEntity.badRequest().body(Map.of("error", "Username exists"));
        if (userRepo.existsByEmail(user.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("error", "Email exists"));
        
        user.setPassword(Base64.getEncoder().encodeToString(user.getPassword().getBytes()));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "Registration successful"));
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req, HttpSession session) {
        String password = Base64.getEncoder().encodeToString(req.get("password").getBytes());
        Optional<User> user = userRepo.findByUsernameOrEmail(req.get("username"), req.get("username"));
        
        if (user.isEmpty() || !user.get().getPassword().equals(password))
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        
        session.setAttribute("userId", user.get().getId());
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "user", Map.of("id", user.get().getId(), "username", user.get().getUsername(), "fullname", user.get().getFullname())
        ));
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        Optional<User> user = userRepo.findById(userId);
        return ResponseEntity.ok(Map.of("user", user.get()));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
    
    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(HttpSession session) {
        if (session.getAttribute("userId") == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        
        List<Post> posts = postRepo.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post post : posts) {
            Optional<User> user = userRepo.findById(post.getUserId());
            result.add(Map.of(
                "id", post.getId(), "image_url", post.getImageUrl(), "caption", post.getCaption(),
                "created_at", post.getCreatedAt(), "username", user.map(u -> u.getUsername()).orElse("Unknown"),
                "fullname", user.map(u -> u.getFullname()).orElse("Unknown")
            ));
        }
        return ResponseEntity.ok(Map.of("posts", result));
    }
    
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> req, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        
        Post post = new Post();
        post.setUserId(userId);
        post.setImageUrl(req.get("image_url"));
        post.setCaption(req.get("caption"));
        postRepo.save(post);
        return ResponseEntity.status(201).body(Map.of("message", "Post created"));
    }
}
