package piotr.messanger.webapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import piotr.messanger.webapp.database.entity.User;
import piotr.messanger.webapp.database.service.ResetTokenService;
import piotr.messanger.webapp.database.service.UserService;
import piotr.messanger.webapp.service.MailingService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;


@Controller
public class LoginController {

    private static final String REGISTRATION = "registration";

    @Resource
    private UserService userService;

    @Resource
    private ResetTokenService tokenService;

    @Resource
    private MailingService mailingService;


    @GetMapping(value={"/", "/login"})
    public ModelAndView login(){

        // temporary change
//        if (hasUserAuthority()) {
//            return new ModelAndView("redirect:/workinprogress");
//        }
        if (hasUserAuthority()) {
            return new ModelAndView("redirect:/messanger");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }


    @GetMapping(value="/registration")
    public ModelAndView registration(){

        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName(REGISTRATION);
        return modelAndView;
    }

    @PostMapping(value = "/registration")
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(REGISTRATION);
        if (bindingResult.hasFieldErrors()) {
            return modelAndView;
        }

        User find = userService.findUserByLogin(user.getLogin());
        if (find != null) {
            bindingResult
                    .rejectValue("login", "error.user",
                            "*This login is already taken");
            return modelAndView;
        }

        find = userService.findUserByEmail(user.getEmail());
        if (find != null) {
            bindingResult
                    .rejectValue("email", "error.user",
                            "*There is already a user registered with the email provided");
            return modelAndView;
        }

        userService.saveUser(user);
        modelAndView.addObject("successMessage", "User has been registered successfully");
        modelAndView.addObject("user", new User());

        return modelAndView;
    }

    @GetMapping(value="/resetPassword")
    public ModelAndView getResetPassword(){

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("resetPassword");
        return modelAndView;
    }

    @PostMapping(value = "/resetPassword")
    public ModelAndView postResetPassword(HttpServletRequest request,
                                          @RequestParam("email") String email) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("resetPassword");
        User user = userService.findUserByEmail(email);
        if (user == null) {
            modelAndView.addObject("errorMessage", "Sorry, no user associated with this email");
            return modelAndView;
        }

        String token = UUID.randomUUID().toString();
        tokenService.addPasswordResetTokenForUser(token, user.getId());

        String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        mailingService.sendResetTokenEmail(appUrl, token, user);

        modelAndView.addObject("successMessage", "Email with reset link <br/> was sent to " + email);
        return modelAndView;
    }

    @GetMapping(value = "/changePassword")
    public String handleChangePassword(@RequestParam("id") long id,
                                             @RequestParam("token") String token,
                                             RedirectAttributes attributes) {

        String result = tokenService.validatePasswordResetToken(id, token);
//        String result = "VERY BAD TOKEN";
        if (result != null) {
            attributes.addFlashAttribute("serverMessage", result);
            return "redirect:/login";

        }
        return "redirect:/updatePassword";
    }


    @GetMapping(value = "/updatePassword")
    public ModelAndView updatePassword() {
        ModelAndView modelAndView = new ModelAndView("/updatePassword");
        modelAndView.setViewName("updatePassword");
        return modelAndView;
    }


    @GetMapping(value = "/messanger")
    public ModelAndView messanger() {

//        // temporary change
//        if (hasUserAuthority()) {
//            return new ModelAndView("redirect:/workinprogress");
//        }


        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("userName", user.getLogin());
        modelAndView.setViewName("messanger");
        return modelAndView;
    }

    private boolean hasUserAuthority() {
        Object[] auth = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray();
        SimpleGrantedAuthority authority = (SimpleGrantedAuthority) auth[0];
        return authority.getAuthority().equals("USER");
    }
}
