package com.music.View.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.music.View.controller.beans.UserBean;
import com.music.View.domain.Cart;
import com.music.View.domain.Product;
import com.music.View.domain.User;
import com.music.View.service.data.CartItemData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;


@Controller
@SessionAttributes({"cart","user"})
public class ViewController {
	
	@Autowired
	private RestTemplate restTemplate;

	@RequestMapping("/")
	public String handleWelcome() {
		return "/catalog";
	}
	
	@ModelAttribute("cart")
	public Cart getCart(HttpServletRequest request) {
		return new Cart(request.getRemoteAddr());
	}
	
	@ModelAttribute("user")
	public UserBean getuser(HttpServletRequest request) {
		return new UserBean(request.getRemoteAddr());
	}
	
	@RequestMapping("/AddToCart")
    public String addtoCart(HttpServletRequest request, @RequestParam(value="quantity", required=false) Integer productQuantity
            , @RequestParam(value = "productCode", required=false) String productCode
            ,@ModelAttribute("cart") Cart cart
            ,@ModelAttribute("user") UserBean user
            ,RedirectAttributes attribute) throws ServletException{
		
        if(user==null) {
        	return "/userWelcome";
        }
        Product product = null;

        try {
            String uri = "http://catalogservice/getProductByCode/" + productCode;
            ResponseEntity<Product> result = restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Product>() {
            });
            //product = catalogService.getProductByCode(productCode);
            product = result.getBody();
        } catch (Exception e) {
            throw new ServletException("productCode not found" + e);
        }

        try {
            cart = (Cart) request.getSession().getAttribute("cart");
//            if (checkCart(request) == false) {
//                ResponseEntity<Cart> getCart = restTemplate.exchange("http://catalogservice/createCart", HttpMethod.GET,
//                        null, new ParameterizedTypeReference<Cart>() {
//                        });
//                cart = getCart.getBody();
                //cart = catalogService.createCart();
                request.getSession().setAttribute("cart", cart);
                attribute.addFlashAttribute("cart", cart);
//            }
            HttpEntity<Cart> entity = new HttpEntity<>(cart);
            restTemplate.exchange("http://catalogservice/cart/save/"+product.getId()+"/"+productQuantity
            ,HttpMethod.POST, entity, Cart.class);
            //catalogService.addItemtoCart(product.getId(), cart, productQuantity);
        }catch(Exception e) {
            throw new ServletException(e);
        }

        return "catalog";
    }
	
    @RequestMapping("/cart.html")
    public String showcart(Model model,HttpServletRequest request
    		,@ModelAttribute("cart") Cart cart
    		,@ModelAttribute("user") UserBean user) throws ServletException {

    	
        Set<CartItemData> setofcartdata = new HashSet<CartItemData>();

        try {
            cart = (Cart) request.getSession().getAttribute("cart");
            //cart.getItems().forEach(i->System.out.println(i.getProductId()));
            HttpEntity<Cart> entity = new HttpEntity<>(cart);
            ResponseEntity<Set<CartItemData>> response = restTemplate.exchange("http://catalogservice/cart/data", HttpMethod.GET
                                                            , entity, new ParameterizedTypeReference<Set<CartItemData>>(){
            });
            setofcartdata = response.getBody();
//            setofcartdata = catalogService.getCartInfo(cart);
        }catch(Exception e) {
            System.out.println("Exception in setofcartdata" + e);
        }
        model.addAttribute("products", setofcartdata);

        BigDecimal total = GetSubTotal(0,setofcartdata);

        model.addAttribute("Total", total);
        return "cart";
    }

    @RequestMapping("/checkout")
    public String checkout(HttpServletRequest request, Model model
    		, @ModelAttribute("cart") Cart cart) throws ServletException{
//        cart = (Cart) request.getSession().getAttribute("cart");
        Set<CartItemData> allproducts = new HashSet<>();

        try{
            HttpEntity<Cart> e = new HttpEntity<>(cart);
            ResponseEntity<Set<CartItemData>> response = restTemplate.exchange("http://catalogservice/cart/data", HttpMethod.GET
                    , e, new ParameterizedTypeReference<Set<CartItemData>>(){
                    });
            allproducts = response.getBody();
//            allproducts = catalogService.getCartInfo(cart);
        }catch(Exception e) {
            System.out.println("Exception in updating cart: " + e);
        }

        System.out.println("Checkout initiated" + cart.getItems().size());

        if(cart.getItems().size() == 0) return "catalog";
        model.addAttribute("Products", allproducts);

        BigDecimal total = GetSubTotal(0,allproducts);
        model.addAttribute("total", total);

        return "checkout";
    }

    @RequestMapping("/Update/{id}")
    public String updateQuantity(HttpServletRequest request, Model model, @PathVariable("id") Integer id,
                                 @RequestParam(value="newQuantity", required=false) Integer quantity
                                 , @ModelAttribute("cart") Cart cart) throws ServletException{

        System.out.println(quantity);
//        Cart cart = (Cart) request.getSession().getAttribute("cart");
        Set<CartItemData> set = new HashSet<>();

        String uri = "http://catalogservice/cart/update/"+id+"/"+quantity;
        HttpEntity<Cart> entity = new HttpEntity<>(cart);
        restTemplate.exchange(uri, HttpMethod.PUT, entity, Cart.class);
//        catalogService.changeCart(id, cart, quantity);

        try{
            HttpEntity<Cart> e = new HttpEntity<>(cart);
            ResponseEntity<Set<CartItemData>> response = restTemplate.exchange("http://catalogservice/cart/data", HttpMethod.GET
                    , e, new ParameterizedTypeReference<Set<CartItemData>>(){
                    });
            set = response.getBody();
//            set = catalogService.getCartInfo(cart);
        }catch(Exception e) {
            System.out.println("Exception in updating cart: " + e);
        }

        model.addAttribute("products", set);

        BigDecimal total = GetSubTotal(0,set);

        model.addAttribute("Total", total);

        return "cart";
    }

    @RequestMapping("/remove/{ID}")
    public String removeCartItem(HttpServletRequest request, Model model, @PathVariable("ID") Integer Id
    		, @ModelAttribute("cart") Cart cart) throws ServletException {
//        Cart cart = (Cart) request.getSession().getAttribute("cart");

        HttpEntity<Cart> e = new HttpEntity<>(cart);
        restTemplate.exchange("http://catalogservice/cart/remove"+Id, HttpMethod.DELETE
                , e, new ParameterizedTypeReference<Set<CartItemData>>(){
                });

//        catalogService.removeCartItem(Id, cart);

        Set<CartItemData> setofcartdata = new HashSet<CartItemData>();

        try {
            HttpEntity<Cart> entity = new HttpEntity<>(cart);
            ResponseEntity<Set<CartItemData>> response = restTemplate.exchange("http://catalogservice/cart/data", HttpMethod.GET
                    , entity, new ParameterizedTypeReference<Set<CartItemData>>(){
                    });
            setofcartdata = response.getBody();
//            setofcartdata = catalogService.getCartInfo(cart);
        }catch(Exception exception) {
            System.out.println("Exception in setofcartdata" + exception);
        }
        model.addAttribute("products", setofcartdata);

        BigDecimal total = GetSubTotal(0,setofcartdata);
        model.addAttribute("Total", total);

        return "cart";
    }

    public static BigDecimal GetSubTotal(int init, Set<CartItemData> set) {
        BigDecimal total = new BigDecimal(init);

        if(set.size()!=0) {
            for(CartItemData item : set) {
                BigDecimal newquantity = new BigDecimal(item.getQuantity());
                total = total.add(item.getPrice().multiply(newquantity));
            }
        }
        System.out.println(total);
        return total;
    }

    private boolean checkCart(HttpServletRequest request) throws IOException {
        HttpSession session = request.getSession();
        Cart cart = (Cart) session.getAttribute("cart");
        return (cart != null);
    }

}
