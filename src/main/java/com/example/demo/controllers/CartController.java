package com.example.demo.controllers;

import com.example.demo.entities.*;
import com.example.demo.services.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductServices productServices;

    @Autowired
    private OrderServices orderServices;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/userLogin";
        }

        List<CartItem> cart = getCartFromSession(session);
        double subtotal = 0;
        for (CartItem item : cart) {
            subtotal += item.getProduct().getPprice() * item.getQuantity();
        }
        double delivery = subtotal > 0 ? 40.0 : 0;
        double total = subtotal + delivery;

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("delivery", delivery);
        model.addAttribute("total", total);
        model.addAttribute("name", loggedInUser.getUname());

        return "Cart";
    }

    @PostMapping("/add/{productId}")
    @ResponseBody
    public String addToCart(@PathVariable("productId") int productId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "UNAUTHORIZED";
        }

        Product product = productServices.getProduct(productId);
        if (product == null) {
            return "NOT_FOUND";
        }

        List<CartItem> cart = getCartFromSession(session);
        boolean exists = false;
        for (CartItem item : cart) {
            if (item.getProduct().getPid() == productId) {
                item.setQuantity(item.getQuantity() + 1);
                exists = true;
                break;
            }
        }

        if (!exists) {
            cart.add(new CartItem(product, 1));
        }

        session.setAttribute("cart", cart);
        return "SUCCESS";
    }

    @PostMapping("/update/{productId}")
    public String updateQuantity(@PathVariable("productId") int productId, @RequestParam("quantity") int quantity, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/userLogin";
        }

        List<CartItem> cart = getCartFromSession(session);
        for (CartItem item : cart) {
            if (item.getProduct().getPid() == productId) {
                if (quantity > 0) {
                    item.setQuantity(quantity);
                } else {
                    cart.remove(item);
                }
                break;
            }
        }

        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable("productId") int productId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/userLogin";
        }

        List<CartItem> cart = getCartFromSession(session);
        cart.removeIf(item -> item.getProduct().getPid() == productId);
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/userLogin";
        }

        List<CartItem> cart = getCartFromSession(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        double totalOrderAmount = 0;
        for (CartItem item : cart) {
            Orders order = new Orders();
            order.setoName(item.getProduct().getPname());
            order.setoPrice(item.getProduct().getPprice());
            order.setoQuantity(item.getQuantity());
            double itemTotal = item.getProduct().getPprice() * item.getQuantity();
            order.setTotalAmmout(itemTotal);
            order.setUser(loggedInUser);
            order.setOrderDate(new Date());

            orderServices.saveOrder(order);
            totalOrderAmount += itemTotal;
        }

        // Add delivery charge
        totalOrderAmount += 40.0;

        // Clear the cart
        session.removeAttribute("cart");

        model.addAttribute("amount", totalOrderAmount);
        return "Order_success";
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCartFromSession(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
}
