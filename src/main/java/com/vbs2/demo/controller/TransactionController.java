package com.vbs2.demo.controller;

import com.vbs2.demo.dto.TransactionDto;
import com.vbs2.demo.models.Transaction;
import com.vbs2.demo.models.User;
import com.vbs2.demo.repositories.TransactionRepo;
import com.vbs2.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    UserRepo userRepo;

    @Autowired
    TransactionRepo transactionRepo;

    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto obj)
    {
        User user = userRepo.findById(obj.getId())
                .orElseThrow(() -> new RuntimeException("not found"));
        double newBalance = user.getBalance() + obj.getAmount();
        user.setBalance(newBalance);
        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs "+obj.getAmount()+"Deposit Successfull");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "Deposit Successful";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto obj)
    {
        User user = userRepo.findById(obj.getId())
                .orElseThrow(() -> new RuntimeException("not found"));
        double newBalance = user.getBalance() - obj.getAmount();
        if(newBalance < 0)
        {
            return "Balance Insufficient";
        }
        user.setBalance(newBalance);
        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs "+obj.getAmount()+"Withdrawal Successfull");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "Withdrawal Successful";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransactionDto obj)
    {
        User sender = userRepo.findById(obj.getId())
                .orElseThrow(() -> new RuntimeException("Not Found"));
        User rec = userRepo.findByUsername(obj.getUsername());

        if(rec==null) return "User not found";
        if(sender.getId()==rec.getId()) return "Self Transaction Not Allowed";
        if(obj.getAmount()<1) return "Invalid Amount";
        double sbalance = sender.getBalance() - obj.getAmount();
        double rbalance = rec.getBalance() + obj.getAmount();

        if(sbalance<0) return "Balance Insufficient";

        sender.setBalance(sbalance);
        rec.setBalance(rbalance);

        userRepo.save(sender);
        userRepo.save(rec);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();

        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sbalance);
        t1.setDescription("Rs "+obj.getAmount()+"Sent to user" +rec.getUsername());
        t1.setUserId(rec.getId());

        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(sbalance);
        t2.setDescription("Rs "+obj.getAmount()+"Recievedd from user" +rec.getUsername());
        t2.setUserId(rec.getId());

        transactionRepo.save(t1);
        transactionRepo.save(t2);
        return "Transfer Done Successfully";
    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> getpassbook(@PathVariable int id)
    {
        return transactionRepo.findAllByUserId(id);
    }
}
