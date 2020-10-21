package com.music.View.controller;

import com.music.View.controller.beans.AdminBean;
import com.music.View.controller.beans.UserBean;
import com.music.View.domain.Invoice;
import com.music.View.service.data.DownloadData;
import com.music.View.service.data.InvoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.springframework.http.HttpMethod;
import java.util.Set;

@Controller
@SessionAttributes("admin")
@RequestMapping("/adminController")
public class AdminController {

    private RestTemplate restTemplate;

    public static final String Admin_jsp_dir = "/admin/";

    @Autowired
    public AdminController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @ModelAttribute("admin")
    public AdminBean getadmin() {
    	return new AdminBean();
    }

    @RequestMapping("/adminPortalLogin.html")
    public String welcomeadmin(HttpServletRequest request, @ModelAttribute("admin") AdminBean admin) throws ServletException {
        String url = Admin_jsp_dir + "adminPortalLogin";
        if (request.getSession().getAttribute("admin") != null || admin!=null) {
            return Admin_jsp_dir + "AdminPage";
        }
        return url;
    }

    @RequestMapping("/AdminCredentials")
    public String displayWelcome(Model model, @RequestParam(value = "firstName", required = false) String firstname,
                                 @RequestParam(value = "password", required = false) String password
                                 , HttpServletRequest request
                                 ,@ModelAttribute("admin") AdminBean admin
                                 , RedirectAttributes attribute) throws ServletException {

        System.out.println("Starting admin detail checkup:" + firstname + "\t" + password);

        //AdminBean admin = (AdminBean) request.getSession().getAttribute("admin");
        //System.out.println(admin);
        if (admin == null)
            admin = new AdminBean();
        if (admin != null)
            admin.setFirstname(firstname);
        if (admin.getfirstname() != null)
            request.getSession().setAttribute("admin", admin);
        
        attribute.addFlashAttribute("admin", admin);
        	
        try {
        	System.out.println(admin);
            System.out.println("Entering the condition:");
            String uri = "http://salesservice/adminController/admin/validate/"+firstname+"/"+password;
            ResponseEntity<Boolean> bool = restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<Boolean>() {
													});
            if (bool.getBody() == true) {
                System.out.println("Successfully entered:");
                System.out.println("Successful Login:-");
                model.addAttribute("firstName", firstname);
                return Admin_jsp_dir + "AdminPage";
            } else {
                System.out.print("Wrong Admin Details:");
                return Admin_jsp_dir + "adminPortalLogin";
            }
        } catch (Exception e) {
            throw new ServletException("Problem with direction"+e);
        }
    }

    @RequestMapping("/ShowReport.html")
    public String DisplayReport(Model model, HttpServletRequest request
    		,@ModelAttribute("admin") AdminBean admin) {
    	
        if (request.getSession().getAttribute("admin") != null || admin!=null) {
        	return Admin_jsp_dir + "ShowReport";
        }
        return Admin_jsp_dir + "adminPortalLogin";
    }

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @RequestMapping("/listVariables.html")
    public String listVariables(Model model, HttpServletRequest request) {
        model.addAttribute("dbUrl", dbUrl);
        System.out.println("dbUrl from application.properties: " + dbUrl);
        String url = Admin_jsp_dir + "listVariables";
        return url;
    }

    @RequestMapping("/logout.html")
    public String logout(Model model, HttpServletRequest request) {
        model.addAttribute("admin", null);
        request.getSession().invalidate();
        request.removeAttribute("admin");
        String url = Admin_jsp_dir+"logout";
        return url;
    }

    @RequestMapping("/AdminPage.html")
    public String PageTesting(HttpServletRequest request, @ModelAttribute("admin") AdminBean admin) {
        if (request.getSession().getAttribute("admin") != null || admin!=null) {
        	String url = Admin_jsp_dir + "AdminPage";
            return url;
        }
        return Admin_jsp_dir + "adminPortalLogin";
    }

    @RequestMapping("/ListOfDownloads.html")
    public String showAllDownloads(Model model, @RequestParam(value = "command", required = false) String command
            , HttpServletRequest request
            ,@ModelAttribute("admin") AdminBean admin) {
        if (request.getSession().getAttribute("admin") == null && admin==null) {
            return Admin_jsp_dir + "adminPortalLogin";
        }
        Set<DownloadData> allDownload = null;
        String url = null;
        try {
            String theUrl = "http://catalogservice/getAllDownloads";

            ResponseEntity<Set<DownloadData>> response = restTemplate.exchange(theUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Set<DownloadData>>() {
            });

            allDownload = response.getBody();

        } catch (Exception e) {
            System.out.println(e);
        }

        if (allDownload != null) {
            model.addAttribute("allDown", allDownload);
            url = Admin_jsp_dir + "ListOfDownloads";
        } else {
            String nothing = "There are no downloads to show";
            model.addAttribute("nothing", nothing);
            url = Admin_jsp_dir + "ListOfDownloads";
        }
        return url;
    }

    @RequestMapping("/process")
    public String Process(Model model, @RequestParam(value = "id", required = false) String id
    		, HttpServletRequest request
    		,@ModelAttribute("admin") AdminBean admin) {
        if (request.getSession().getAttribute("admin") == null && admin==null) {
            return Admin_jsp_dir + "adminPortalLogin";
        }
        String url = null;
        System.out.println("Processing invoice with id :" + id);
        long id1 = Long.parseLong(id);
        try {
            if (id != null) {
                restTemplate.exchange("http://salesservice/adminController/update/invoice/"+id, HttpMethod.PUT, null, Invoice.class);
                //salesService.processInvoice(id1);
                url = Admin_jsp_dir + "ProcessInvoice";
            } else {
                url = "forward:processInvoice";
            }
        } catch (Exception e) {
            System.out.println(e);
            url = "forward:processInvoice";
        }
        return url;
    }

    @RequestMapping("/processInvoice")
    public String ProcessInvoice(Model model, @RequestParam(value = "command", required = false) String command
    		, HttpServletRequest request
    		, @ModelAttribute("admin") AdminBean admin) {
    	
        if (request.getSession().getAttribute("admin") == null && admin==null) {
            return Admin_jsp_dir + "adminPortalLogin";
        }
        Set<InvoiceData> invoices = null;
        String ForwardUrl = null;


        try {
            ResponseEntity<Set<InvoiceData>> response = restTemplate.exchange("http://salesservice/adminController/invoice/unprocessed",
                    HttpMethod.GET, null, new ParameterizedTypeReference<Set<InvoiceData>>() {
                    });
            invoices = response.getBody();
            //invoices = salesService.getListofUnprocessedInvoices();

        } catch (Exception e) {
            System.out.println(e);
        }

        if (invoices != null && command == null) {
            model.addAttribute("Invoices", invoices);
            ForwardUrl = Admin_jsp_dir + "ProcessInvoice";
        } else {
            String nothing = "There are no downloads to show";
            model.addAttribute("nothing", nothing);
            ForwardUrl = Admin_jsp_dir + "ProcessInvoice";
        }


        return ForwardUrl;
    }


    @RequestMapping("/forinvoiceprocess.html")
    public String ToProcessInvoice(Model model
    		, @RequestParam(value = "command", required = false) String command
    		, HttpServletRequest request
    		, @ModelAttribute("admin") AdminBean admin) {
    	
        if (request.getSession().getAttribute("admin") == null && admin==null) {
            return Admin_jsp_dir + "adminPortalLogin";
        }
        Set<InvoiceData> invoices = null;
        String url = null;
        try {

            ResponseEntity<Set<InvoiceData>> response = restTemplate.exchange("http://salesservice/adminController/invoice/unprocessed",
                    HttpMethod.GET, null, new ParameterizedTypeReference<Set<InvoiceData>>() {
                    });
            invoices = response.getBody();
            //invoices = salesService.getListofUnprocessedInvoices();

        } catch (Exception e) {
            System.out.println(e);
        }

        if (invoices != null && command == null) {
            model.addAttribute("Invoices", invoices);
            url = Admin_jsp_dir + "forinvoiceprocess";

        } else {
            String nothing = "There are no downloads to show";
            model.addAttribute("nothing", nothing);
            url = Admin_jsp_dir + "forinvoiceprocess";
        }

        return url;
    }
}
