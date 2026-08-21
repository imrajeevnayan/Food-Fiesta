package com.example.demo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.demo.entities.Admin;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;

@Component
public class UserServices 
{
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public List<User> getAllUser()
	{
		List<User> users = (List<User>) this.userRepository.findAll();
		return users;
	}

	public User getUser(int id)
	{
		Optional<User> optional = this.userRepository.findById(id);
		User user = optional.get();
		return user;
	}
	public User getUserByEmail(String email)
	{
	 User user=	this.userRepository.findUserByUemail(email);
	 return user;
	}

	public void updateUser(User user,int id)
	{
		user.setU_id(id);
		if (user.getUpassword() != null && !user.getUpassword().isEmpty()) {
			user.setUpassword(passwordEncoder.encode(user.getUpassword()));
		}
		this.userRepository.save(user);
	}

	public void deleteUser(int id)
	{
		this.userRepository.deleteById(id);
	}

	public void addUser(User user)
	{
		if (user.getUpassword() != null && !user.getUpassword().isEmpty()) {
			user.setUpassword(passwordEncoder.encode(user.getUpassword()));
		}
		this.userRepository.save(user);
	}
	
	public boolean validateLoginCredentials(String email,String password)
	{
		List<User> users = (List<User>) this.userRepository.findAll();
		for(User u:users)
		{
		if(u!=null && u.getUemail().equals(email) && passwordEncoder.matches(password, u.getUpassword()))
		{
			return true;
		}
		}
		return false;
	}
	


}