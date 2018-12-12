package piotr.messanger.webapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import piotr.messanger.webapp.database.entity.User;
import piotr.messanger.webapp.database.service.UserService;

import javax.annotation.Resource;
import javax.validation.Valid;


@Controller
public class LoginController {

    @Resource
    private UserService userService;

    private static final String REGISTRATION = "registration";

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

//    @GetMapping(value = "/workinprogress")
//    public ModelAndView workInProgress() {
//        ModelAndView modelAndView = new ModelAndView();
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        User user = userService.findUserByEmail(auth.getName());
//        modelAndView.addObject("userName", user.getLogin());
//        modelAndView.setViewName("workinprogress");
//        return modelAndView;
//    }

    private boolean hasUserAuthority() {
        Object[] auth = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray();
        SimpleGrantedAuthority authority = (SimpleGrantedAuthority) auth[0];
        return authority.getAuthority().equals("USER");
    }
}
