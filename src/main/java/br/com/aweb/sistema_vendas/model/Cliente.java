package br.com.aweb.sistema_vendas.model;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome completo é obrigatório.")
    @Column(nullable = false, length = 100)
    private String nomeCompleto;

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @NotBlank(message = "CPF é obrigatório.")
    @CPF(message = "Informe um CPF válido.")
    @Column(nullable = false, length = 14, unique = true)
    private String cpf;

    @NotBlank(message = "Telefone é obrigatório.")
    @Column(nullable = false, length = 20)
    private String telefone;

    @NotBlank(message = "Logradouro é obrigatório.")
    @Column(nullable = false, length = 150)
    private String logradouro;

    @Column(length = 10)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @NotBlank(message = "Bairro é obrigatório.")
    @Column(nullable = false, length = 100)
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória.")
    @Column(nullable = false, length = 100)
    private String cidade;

    @NotBlank(message = "UF é obrigatória.")
    @Pattern(regexp = "[A-Za-z]{2}", message = "A UF deve conter 2 letras.")
    @Column(nullable = false, length = 2)
    private String uf;

    @NotBlank(message = "CEP é obrigatório.")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Informe um CEP válido (00000-000).")
    @Column(nullable = false, length = 9)
    private String cep;

    public String getEnderecoCompleto() {
        var endereco = new StringBuilder(logradouro);
        if (numero != null && !numero.isBlank())
            endereco.append(", ").append(numero);
        if (complemento != null && !complemento.isBlank())
            endereco.append(" - ").append(complemento);
        endereco.append(", ").append(bairro);
        endereco.append(", ").append(cidade).append("/").append(uf);
        endereco.append(" - CEP ").append(cep);
        return endereco.toString();
    }

}
