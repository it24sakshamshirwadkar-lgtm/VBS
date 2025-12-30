package com.vbs2.demo.controller;

import com.vbs2.demo.dto.DisplayDto;
import com.vbs2.demo.dto.LoginDto;
import com.vbs2.demo.dto.UpdateDto;
import com.vbs2.demo.models.History;
import com.vbs2.demo.models.Transaction;
import com.vbs2.demo.models.User;
import com.vbs2.demo.repositories.HistoryRepo;
import com.vbs2.demo.repositories.TransactionRepo;
import com.vbs2.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;
    @Autowired
    TransactionRepo transactionRepo;

    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        userRepo.save(user);
        return "Signup Successful";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto u)
    {
        User user = userRepo.findByUsername(u.getUsername());
        if(user==null)
        {
            return "User Not Found";
        }

        if(!u.getPassword().equals(user.getPassword()))
        {
            return "Password is Incorrect";
        }
        if(!u.getRole().equals(user.getRole()))
        {
            return "Role is Incorrect";
        }
        return String.valueOf(user.getId());
    }

    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("User Not Found"));
        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        User user = userRepo.findById(obj.getId()).orElseThrow(()-> new RuntimeException("Not Found"));
        if(obj.getKey().equalsIgnoreCase("name"))
        {
            if(obj.getValue().equals(user.getName())) return "Name Cannot be Same";
            user.setName(obj.getValue());
        }

        else if(obj.getKey().equalsIgnoreCase("password"))
        {
            if(obj.getValue().equals(user.getPassword())) return "Password Cannot be Same";
            user.setPassword(obj.getValue());
        }

        else if(obj.getKey().equalsIgnoreCase("email"))
        {
            if(obj.getValue().equals(user.getEmail())) return "Email Cannot be Same";
            User user2 = userRepo.findByEmail(obj.getValue());
            if(user2 !=null) return "Email Already Exists";
            user.setEmail(obj.getValue());
        }

        userRepo.save(user);
        return "Updated Successfully";

    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable int adminId)
    {
        History h1 = new History();
        h1.setDescription("Admin "+adminId+" Created User "+user.getUsername());
        historyRepo.save(h1);
        userRepo.save(user);

        if(user.getBalance()>0)
        {
            User user2 = userRepo.findByUsername(user.getUsername());
            Transaction t = new Transaction();
            t.setAmount(user.getBalance());
            t.setCurrBalance(user.getBalance());
            t.setDescription("Rs "+user.getBalance()+" Deposited Successful");
            t.setUserId(user2.getId());
            transactionRepo.save(t);
        }
        return "Added Successfully";
    }


    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String delete(@PathVariable int userId, @PathVariable int adminId)
    {
        User user= userRepo.findById(userId).orElseThrow(()-> new RuntimeException("Not Found"));
        if(user.getBalance()>0)
        {
            return "Balance should be 0";
        }

        History h1= new History();
        h1.setDescription("Admin "+adminId+" Deleted User "+user.getUsername());
        historyRepo.save(h1);
        userRepo.delete(user);
        return "User Deleted Successfully";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy,@RequestParam String order )
    {
        Sort sort;
        if(order.equalsIgnoreCase("desc"))
        {
            sort=Sort.by(sortBy).descending();
        }

        else
        {
            sort=Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("customer",sort);
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUser(@PathVariable String keyword)
    {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }
}