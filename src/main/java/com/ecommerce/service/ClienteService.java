package com.ecommerce.service;

import com.ecommerce.entity.Cliente;
import com.ecommerce.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Escrita — vai para o banco PRIMARIO.
     */
    @Transactional
    public Cliente cadastrar(String nome, String email) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setEmail(email);
        Cliente salvo = clienteRepository.save(cliente);
        System.out.printf("[ESCRITA → PRIMARY] Cliente cadastrado: id=%d | nome=%s | email=%s%n",
                salvo.getId(), salvo.getNome(), salvo.getEmail());
        return salvo;
    }

    /**
     * Leitura — vai para a REPLICA (round-robin entre as N replicas).
     */
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }
}
