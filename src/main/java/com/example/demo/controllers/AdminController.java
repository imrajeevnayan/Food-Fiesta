package com.example.demo.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.count.*;
import com.example.demo.entities.*;
import com.example.demo.loginCredentials.*;
import com.example.demo.services.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "Admin Controller", description = "Back-office and authentication endpoints")
public class AdminController {
	@Autowired
	private UserServices services;
	@Autowired
	private AdminServices adminServices;
	@Autowired
	private ProductServices productServices;	
	@Autowired
	private OrderServices orderServices;

// Removed instance variables email and user to prevent data leakage in singleton controller

	@PostMapping("/adminLogin")
	@Operation(summary = "Admin login authentication", description = "Validates admin credentials and redirects to the services dashboard")
	public String getAllData(@ModelAttribute("adminLogin") AdminLogin login, Model model, jakarta.servlet.http.HttpSession session) {
		String email = login.getEmail();
		String password = login.getPassword();
		System.out.println("DEBUG: Submitted admin login: email=" + email + ", password=" + password);
		System.out.println("DEBUG: Admins in database: " + adminServices.getAll());
		if (adminServices.validateAdminCredentials(email, password)) {
			session.setAttribute("loggedInAdmin", email);
			return "redirect:/admin/services";
		} else {
			model.addAttribute("error", "Invalid email or password");
			return "Login";
		}
	}

	@PostMapping("/userLogin")
	@Operation(summary = "User login authentication", description = "Validates user credentials and redirects to the dashboard")
	public String userLogin(@ModelAttribute("userLogin") UserLogin login, Model model, jakarta.servlet.http.HttpSession session) {
		String email = login.getUserEmail();
		String password = login.getUserPassword();
		if (services.validateLoginCredentials(email, password)) {
			User loggedInUser = this.services.getUserByEmail(email);
			session.setAttribute("loggedInUser", loggedInUser);
			return "redirect:/dashboard";
		} else {
			model.addAttribute("error2", "Invalid email or password");
			return "Login";
		}
	}

	@GetMapping("/dashboard")
	@Operation(summary = "User Dashboard", description = "Displays the product ordering page for logged-in users")
	public String dashboard(Model model, jakarta.servlet.http.HttpSession session) {
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if (loggedInUser == null) {
			return "redirect:/userLogin";
		}
		List<Orders> orders = this.orderServices.getOrdersForUser(loggedInUser);
		model.addAttribute("orders", orders);
		model.addAttribute("name", loggedInUser.getUname());
		return "BuyProduct";
	}

	@GetMapping("/logout")
	@Operation(summary = "Logout", description = "Invalidates the session and redirects to home")
	public String logout(jakarta.servlet.http.HttpSession session) {
		session.invalidate();
		return "redirect:/home";
	}
	@PostMapping("/product/search")
	@Operation(summary = "Search for a product", description = "Finds a specific food item by name and returns its details")
	public String seachHandler(@RequestParam("productName") String name, Model model, jakarta.servlet.http.HttpSession session) {
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if (loggedInUser == null) {
			return "redirect:/userLogin";
		}

		Product product = this.productServices.getProductByName(name);
		List<Orders> orders = this.orderServices.getOrdersForUser(loggedInUser);
		model.addAttribute("orders", orders);
		model.addAttribute("name", loggedInUser.getUname());

		if (product == null) {
			model.addAttribute("message", "SORRY...! Product '" + name + "' Unavailable");
			model.addAttribute("product", null);
		} else {
			model.addAttribute("product", product);
		}
		return "BuyProduct";
	}
	@GetMapping("/admin/services")
	@Operation(summary = "Admin Services Dashboard", description = "Displays the overview of all users, admins, products, and orders")
	public String returnBack(Model model, jakarta.servlet.http.HttpSession session) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/login";
		}
		List<User> users = this.services.getAllUser();
		List<Admin> admins = this.adminServices.getAll();
		List<Product> products = this.productServices.getAllProducts();
		List<Orders> orders = this.orderServices.getOrders();
		model.addAttribute("users", users);
		model.addAttribute("admins", admins);
		model.addAttribute("products", products);
		model.addAttribute("orders", orders);

		return "Admin_Page";
	}
	@GetMapping("/addAdmin")
	@Operation(summary = "View Add Admin page", description = "Serves the HTML form to create a new administrator account")
	public String addAdminPage()
	{
		return "Add_Admin";
	}
	@PostMapping("addingAdmin")
	@Operation(summary = "Create a new Admin", description = "Processes the form submission to save a new administrator to the database")
	public String addAdmin( @ModelAttribute Admin admin)
	{

		this.adminServices.addAdmin(admin);
		return "redirect:/admin/services";

	}
	@GetMapping("/updateAdmin/{adminId}")
	@Operation(summary = "View Update Admin page", description = "Loads the specified admin's details into the update form")
	public String update(@PathVariable("adminId") int id,Model model)
	{
		Admin admin = this.adminServices.getAdmin(id);
		model.addAttribute("admin", admin);
		return "Update_Admin";
	}
	@GetMapping("/updatingAdmin/{id}")
	@Operation(summary = "Process Admin update", description = "Updates an existing administrator's information in the database")
	public String updateAdmin(@ModelAttribute Admin admin,@PathVariable("id") int id)
	{
		this.adminServices.update(admin, id);
		return "redirect:/admin/services";
	}
	@GetMapping("/deleteAdmin/{id}")
	@Operation(summary = "Delete an Admin", description = "Removes an administrator account by ID")
	public String deleteAdmin(@PathVariable("id") int id)
	{
		this.adminServices.delete(id);
		return "redirect:/admin/services";
	}
	@GetMapping("/addProduct")
	@Operation(summary = "View Add Product page", description = "Serves the HTML form to register a new food item")
	public String addProduct()
	{
		return "Add_Product";
	}
	
	@GetMapping("/updateProduct/{productId}")
	@Operation(summary = "View Update Product page", description = "Loads the specified food item's details into the edit form")
	public String updateProduct(@PathVariable("productId") int id,Model model)
	{
		Product product=this.productServices.getProduct(id);
		System.out.println(product);
		model.addAttribute("product", product);
		return "Update_Product";
	}

	@GetMapping("/addUser")
	@Operation(summary = "View Add User page", description = "Serves the HTML form to register a new customer manually")
	public String addUser()
	{
		return "Add_User";
	}

	@GetMapping("/updateUser/{userId}")
	@Operation(summary = "View Update User page", description = "Loads the specified customer's details into the edit form")
	public String updateUserPage(@PathVariable("userId") int id,Model model)
	{
		User user = this.services.getUser(id);
		model.addAttribute("user", user);
		return "Update_User";
	}

	@PostMapping("/product/order")
	@Operation(summary = "Process food order", description = "Calculates total amount and saves the order in the system")
	public String orderHandler(@ModelAttribute() Orders order, Model model, jakarta.servlet.http.HttpSession session) {
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		if (loggedInUser == null) {
			return "redirect:/userLogin";
		}

		double totalAmount = Logic.countTotal(order.getoPrice(), order.getoQuantity());
		order.setTotalAmmout(totalAmount);
		order.setUser(loggedInUser);
		order.setOrderDate(new Date());
		this.orderServices.saveOrder(order);
		model.addAttribute("amount", totalAmount);
		return "Order_success";
	}

	@GetMapping("/product/back")
	@Operation(summary = "Return to shop", description = "Navigates back to the product catalog after viewing orders")
	public String back(jakarta.servlet.http.HttpSession session) {
		return "redirect:/dashboard";
	}

}