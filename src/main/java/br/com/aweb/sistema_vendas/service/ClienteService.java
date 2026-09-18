package br.com.aweb.sistema_vendas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.aweb.sistema_vendas.model.Cliente;
import br.com.aweb.sistema_vendas.repository.ClienteRepository;
import jakarta.transaction.Transactional;

@Service
public class ClienteService {

    @Autowired
    ClienteRepository clienteRepository;

    // CREATE
    @Transactional
    public Cliente salvar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    // READ
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    // UPDATE
    @Transactional
    public Cliente atualizar(Long id, Cliente clienteAtualizado) {
        var optionalCliente = buscarPorId(id);
        if (!optionalCliente.isPresent())
            throw new IllegalArgumentException("Cliente não encontrado.");

        var clienteExistente = optionalCliente.get();

        clienteExistente.setNomeCompleto(clienteAtualizado.getNomeCompleto());
        clienteExistente.setEmail(clienteAtualizado.getEmail());
        clienteExistente.setCpf(clienteAtualizado.getCpf());
        clienteExistente.setTelefone(clienteAtualizado.getTelefone());
        clienteExistente.setLogradouro(clienteAtualizado.getLogradouro());
        clienteExistente.setNumero(clienteAtualizado.getNumero());
        clienteExistente.setComplemento(clienteAtualizado.getComplemento());
        clienteExistente.setBairro(clienteAtualizado.getBairro());
        clienteExistente.setCidade(clienteAtualizado.getCidade());
        clienteExistente.setUf(clienteAtualizado.getUf());
        clienteExistente.setCep(clienteAtualizado.getCep());

        var clienteSalvo = clienteRepository.save(clienteExistente);
        return clienteSalvo;
    }

    // DELETE
    @Transactional
    public void excluir(Long id) {
        var optionalCliente = buscarPorId(id);
        if (!optionalCliente.isPresent())
            throw new IllegalArgumentException("Cliente não encontrado.");

        clienteRepository.deleteById(id);
    }

    // Unicidade: o e-mail/CPF só pode pertencer ao próprio cliente que está sendo editado
    public boolean emailEmUso(String email, Long id) {
        return clienteRepository.findByEmail(email)
                .filter(cliente -> !cliente.getId().equals(id))
                .isPresent();
    }

    public boolean cpfEmUso(String cpf, Long id) {
        return clienteRepository.findByCpf(cpf)
                .filter(cliente -> !cliente.getId().equals(id))
                .isPresent();
    }

}
