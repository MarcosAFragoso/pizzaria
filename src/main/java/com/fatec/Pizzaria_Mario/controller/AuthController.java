package com.fatec.Pizzaria_Mario.controller;

import com.fatec.Pizzaria_Mario.model.User;
import com.fatec.Pizzaria_Mario.repository.UserRepository;
import com.fatec.Pizzaria_Mario.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository; // Usado para /verificarhashdebug

    @Autowired
    private PasswordEncoder passwordEncoder; // Usado para /verificarhashdebug

    // --- Método para exibir a Página de Login ---
    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @ModelAttribute("successMessage") String successMessage,
                            @ModelAttribute("errorMessage") String flashErrorMessage) { // Renomeado para evitar conflito com param.error

        // Mensagens de erro/sucesso de RedirectAttributes (ex: após registro, reset de senha)
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage);
        }
        if (flashErrorMessage != null && !flashErrorMessage.isEmpty()) {
            model.addAttribute("errorMessage", flashErrorMessage);
        }

        // Mensagens padrão para erro de login e logout (geradas pelo Spring Security como parâmetros de URL)
        if (error != null) {
            model.addAttribute("loginSpecificError", "E-mail ou senha inválidos. Tente novamente.");
        }
        if (logout != null) {
            // A mensagem de logout já está no login.html via th:if="${param.logout}"
            // mas podemos adicionar uma flash attribute se quisermos mais controle
            // model.addAttribute("logoutMessage", "Você foi desconectado com sucesso.");
        }
        return "login"; // Nome do template login.html
    }

    // --- Métodos para Registro ---
    @GetMapping("/registrar")
    public String showRegistrationForm(Model model) {
        return "registrar";
    }

    @PostMapping("/registrar")
    public String processRegistration(@RequestParam("nomeCompleto") String nomeCompleto,
                                      @RequestParam("email") String email,
                                      @RequestParam("senha") String senha,
                                      @RequestParam("confirmarSenha") String confirmarSenha,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("errorMessage", "As senhas não coincidem.");
            model.addAttribute("nomeCompleto", nomeCompleto);
            model.addAttribute("email", email);
            return "registrar";
        }
        try {
            authService.registerUser(nomeCompleto, email, senha);
            redirectAttributes.addFlashAttribute("successMessage", "Cadastro realizado com sucesso! Faça o login.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("nomeCompleto", nomeCompleto);
            model.addAttribute("email", email);
            return "registrar";
        }
    }

    // --- Métodos para Esqueci/Redefinir Senha ---
    @GetMapping("/esqueci-senha")
    public String showForgotPasswordForm() {
        return "esqueci-senha";
    }

    @PostMapping("/esqueci-senha-submit")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            authService.generatePasswordResetToken(email);
            redirectAttributes.addFlashAttribute("successMessage", "Se o e-mail estiver cadastrado, um link de recuperação foi enviado.");
        } catch (Exception e) {
            // Mesmo em caso de erro (ex: falha no envio do e-mail), mostre uma mensagem genérica
            redirectAttributes.addFlashAttribute("successMessage", "Se o e-mail estiver cadastrado, um link de recuperação foi enviado.");
            System.err.println("Erro ao processar esqueci-senha para " + email + ": " + e.getMessage());
        }
        return "redirect:/esqueci-senha";
    }

    @GetMapping("/redefinir-senha")
    public String showResetPasswordForm(@RequestParam(required = false) String token, Model model, RedirectAttributes redirectAttributes) {
        if (token == null || !authService.validatePasswordResetToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Token de redefinição inválido ou expirado.");
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "redefinir-senha";
    }

    @PostMapping("/redefinir-senha-submit")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("novaSenha") String novaSenha,
                                       @RequestParam("confirmarNovaSenha") String confirmarNovaSenha,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (!authService.validatePasswordResetToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Token de redefinição inválido ou expirado.");
            return "redirect:/login";
        }
        if (!novaSenha.equals(confirmarNovaSenha)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "As senhas não coincidem.");
            return "redefinir-senha";
        }
        try {
            authService.resetPassword(token, novaSenha);
            redirectAttributes.addFlashAttribute("successMessage", "Senha redefinida com sucesso! Faça o login com sua nova senha.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Erro ao redefinir senha: " + e.getMessage());
            return "redefinir-senha";
        }
    }

    // --- Métodos para Editar Perfil ---
    @GetMapping("/editar-perfil")
    public String showEditProfileForm(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            return "redirect:/login";
        }
        String userEmail = authentication.getName();
        User user = authService.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + userEmail));
        model.addAttribute("user", user);
        return "editar-perfil";
    }

    @PostMapping("/editar-perfil")
    public String processEditProfile(@ModelAttribute("user") User userFormData,
                                     @RequestParam(value = "senhaAtual", required = false) String senhaAtual,
                                     @RequestParam(value = "novaSenha", required = false) String novaSenha,
                                     @RequestParam(value = "confirmarNovaSenha", required = false) String confirmarNovaSenha,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            return "redirect:/login";
        }
        String userEmailAutenticado = authentication.getName();
        try {
            authService.updateUserProfile(
                    userEmailAutenticado,
                    userFormData.getNomeCompleto(),
                    userFormData.getEmail(),
                    senhaAtual,
                    novaSenha,
                    confirmarNovaSenha
            );
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
            return "redirect:/editar-perfil";
        } catch (IllegalArgumentException e) {
            User currentUser = authService.findByEmail(userEmailAutenticado).orElse(userFormData);
            model.addAttribute("user", currentUser);
            model.addAttribute("errorMessage", e.getMessage());
            return "editar-perfil";
        } catch (Exception e) {
            User currentUser = authService.findByEmail(userEmailAutenticado).orElse(userFormData);
            model.addAttribute("user", currentUser);
            model.addAttribute("errorMessage", "Ocorreu um erro inesperado ao atualizar o perfil.");
            System.err.println("Erro ao atualizar perfil para " + userEmailAutenticado + ": " + e.getMessage());
            e.printStackTrace();
            return "editar-perfil";
        }
    }

    // --- Endpoint de Debug (Remover em Produção) ---
    @GetMapping("/verificarhashdebug")
    @ResponseBody
    public String verificarHashDebug(@RequestParam String email, @RequestParam String senhaPlana) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String senhaDoBanco = user.getSenha();
            boolean senhasCorrespondem = passwordEncoder.matches(senhaPlana, senhaDoBanco);
            StringBuilder result = new StringBuilder();
            result.append("<h2>Debug de Verificação de Senha</h2>");
            result.append("<p><strong>E-mail Testado:</strong> ").append(email).append("</p>");
            result.append("<p><strong>Senha Plana Fornecida:</strong> ").append(senhaPlana).append("</p>");
            result.append("<p><strong>Hash da Senha no Banco:</strong> <span style='word-break:break-all;'>").append(senhaDoBanco).append("</span></p>");
            result.append("<p><strong>PasswordEncoder.matches(senhaPlana, senhaDoBanco):</strong> <strong style='color: ").append(senhasCorrespondem ? "green" : "red").append(";'>").append(senhasCorrespondem).append("</strong></p>");
            if (!senhasCorrespondem) {
                result.append("<hr><p><strong>Tentando gerar hash da senha plana para comparação (APENAS VISUAL):</strong></p>");
                String hashDaSenhaPlana = passwordEncoder.encode(senhaPlana);
                result.append("<p>Hash gerado para '").append(senhaPlana).append("': <span style='word-break:break-all;'>").append(hashDaSenhaPlana).append("</span></p>");
                result.append("<p style='color: red;'>Visualmente, os hashes também são diferentes.</p>");
            }
            return result.toString();
        }
        return "<h2>Debug de Verificação de Senha</h2><p>Usuário não encontrado com o e-mail: " + email + "</p>";
    }
}