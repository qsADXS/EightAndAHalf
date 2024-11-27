package com.eh.user.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotNull
    @Length(min = 2, max = 20)
    private String username;
    @NotNull
    @Length(min = 6, max = 20)
    private String password;
    @NotNull
    @Email
    private String email;
}
