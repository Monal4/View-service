package com.music.View.controller;

import com.music.View.controller.beans.UserBean;
import com.music.View.domain.User;
import com.music.View.service.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

@Controller
@SessionAttributes("user")
public class SalesController {

    private RestTemplate restTemplate;

    @Autowired
    public SalesController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/download")
    public String showProduct(Model model, @RequestParam(value = "productCode", required=false)
            String productCode, HttpServletRequest request
            ,@ModelAttribute("user") UserBean user) throws ServletException
    {
        String url = null;
        if(request.getSession().getAttribute("user") != null || user!=null) {
        	model.addAttribute("productCode", productCode);
            return "/sound/" + productCode + "/sound";
        }
        
        System.out.println("Session" + request.getSession().getAttribute("user"));
    	return "/userWelcome";
    }
    
    @RequestMapping("/userWelcome.html")
    public String welcomeuser(HttpServletRequest request,@ModelAttribute("user") UserBean user) throws ServletException {
        if (request.getSession().getAttribute("user") != null || user!=null) {
            return "catalog";
        }
        return "userWelcome";
    }

    @ModelAttribute("user")
    public UserBean getuser(HttpServletRequest request) {
    	return new UserBean(request.getRemoteAddr());
    }
    
    @RequestMapping("/Registered.html")
    public String displayWelcome(Model model, @RequestParam(value = "email", required = false) String email,
                                 @RequestParam(value = "firstName", required = false) String firstname, 
                                 @RequestParam(value = "lastName", required = false) String lastname,
                                 @RequestParam(value = "address", required = false) String address, HttpServletRequest request
                                 , RedirectAttributes attribute
                                 , @ModelAttribute("user") UserBean user) throws ServletException {

        System.out.println("Starting User registration:");

        //String forwardURL;

        if (user != null) {
            user.setEmail(email);
        }
        
        request.getSession().setAttribute("user", user);
       // System.out.println("Session" + request.getSession().getAttribute("user"));
        attribute.addFlashAttribute("user", user);
        
        //System.out.print("The inserted values are:-" + email + firstname + lastname);
        try {
            String uri = "http://salesservice/user/register/"+firstname+"/"+lastname+"/"+email;
            if(email!=null) {
            	restTemplate.exchange(uri, HttpMethod.POST, null, User.class);
            }
            //salesService.registerUser(firstname, lastname, email);
        } catch (Exception e) {
            System.out.println(e);
            throw new ServletException("Cannot insert user into Database:-");
        }

        try {
            boolean bool = restTemplate.getForObject("http://salesservice/user/customer/"+email, boolean.class);
            if (bool) {
                ResponseEntity<UserData> userdata = restTemplate.exchange("http://salesservice/user/" + email, HttpMethod.GET, null, new ParameterizedTypeReference<UserData>() {
                });
                //salesService.getUserInfoByEmail(email);
            } else {
                model.addAttribute("email", email);
                return "address";
            }
        } catch (Exception e) {
            throw new ServletException("address update problem" + e);
        }

        return "catalog";
    }

    @RequestMapping("addAddress.html")
    public String addAddress(Model model, @RequestParam(value = "email", required = false)
            String email, @RequestParam(value = "address", required = false) String address
            , HttpServletRequest request) throws ServletException {

        UserData user = new UserData();

        try {
            ResponseEntity<UserData> userdata = restTemplate.exchange("http://salesservice/user/" + email, HttpMethod.GET, null, new ParameterizedTypeReference<UserData>() {
            });
            user = userdata.getBody();
            //user = salesService.getUserInfoByEmail(email);
        } catch (Exception e) {
            throw new ServletException("Cannot get info from email");
        }
        long id = user.getId();
        try {
            restTemplate.exchange("http://salesservice/user/"+id+"/"+address, HttpMethod.POST, null, User.class);
            //salesService.addUserAddress(id, address);
        } catch (Exception e) {
            throw new ServletException("Cannot update address");
        }

        return "catalog";
    }
    
    @RequestMapping("address.html")
    public String addressForm() {
        return "address";
    }

    @RequestMapping("catalog.html")
    public String showCatalog(HttpServletRequest request) {
        return "catalog";
    }
}
