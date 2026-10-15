package br.com.aweb.sistema_vendas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.aweb.sistema_vendas.model.ItemPedido;
import br.com.aweb.sistema_vendas.model.Pedido;
import br.com.aweb.sistema_vendas.model.Produto;
import br.com.aweb.sistema_vendas.model.StatusPedido;
import br.com.aweb.sistema_vendas.repository.PedidoRepository;
import jakarta.transaction.Transactional;

@Service
public class PedidoService {

    @Autowired
    PedidoRepository pedidoRepository;

    @Autowired
    ProdutoService produtoService;

    // CREATE
    @Transactional
    public Pedido salvar(Pedido pedido) {
        pedido.setDataHora(LocalDateTime.now());
        pedido.setStatus(StatusPedido.ATIVO);

        var itensDoFormulario = new ArrayList<>(pedido.getItens());
        pedido.setItens(new ArrayList<>());

        for (var item : itensDoFormulario) {
            incluirItem(pedido, item.getProduto().getId(), item.getQuantidade());
        }

        if (pedido.getItens().isEmpty())
            throw new IllegalArgumentException("Adicione pelo menos um produto ao pedido.");

        recalcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }

    // READ
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByDataHoraDesc();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    // UPDATE
    @Transactional
    public Pedido adicionarProduto(Long id, Long produtoId, Integer quantidade) {
        var pedido = buscarPedidoAtivo(id);
        incluirItem(pedido, produtoId, quantidade);
        recalcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido alterarQuantidade(Long id, Long itemId, Integer novaQuantidade) {
        var pedido = buscarPedidoAtivo(id);
        var item = buscarItem(pedido, itemId);

        if (novaQuantidade == null || novaQuantidade <= 0)
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");

        var produto = item.getProduto();
        var diferenca = novaQuantidade - item.getQuantidade();

        if (diferenca > produto.getQuantidadeEmEstoque())
            throw new IllegalArgumentException(mensagemEstoque(produto));

        produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() - diferenca);
        produtoService.salvar(produto);

        item.setQuantidade(novaQuantidade);
        item.recalcularSubtotal();

        recalcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido removerProduto(Long id, Long itemId) {
        var pedido = buscarPedidoAtivo(id);
        var item = buscarItem(pedido, itemId);

        if (pedido.getItens().size() == 1)
            throw new IllegalArgumentException("O pedido deve ter pelo menos um produto.");

        devolverAoEstoque(item);
        pedido.getItens().remove(item);

        recalcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }

    // CANCEL
    @Transactional
    public Pedido cancelar(Long id) {
        var pedido = buscarPedidoAtivo(id);

        pedido.getItens().forEach(this::devolverAoEstoque);
        pedido.setStatus(StatusPedido.CANCELADO);

        return pedidoRepository.save(pedido);
    }

    private void incluirItem(Pedido pedido, Long produtoId, Integer quantidade) {
        if (produtoId == null)
            throw new IllegalArgumentException("Selecione um produto.");

        if (quantidade == null || quantidade <= 0)
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");

        var optionalProduto = produtoService.buscarPorId(produtoId);
        if (!optionalProduto.isPresent())
            throw new IllegalArgumentException("Produto não encontrado.");

        var produto = optionalProduto.get();
        if (quantidade > produto.getQuantidadeEmEstoque())
            throw new IllegalArgumentException(mensagemEstoque(produto));

        var itemExistente = pedido.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            var item = itemExistente.get();
            item.setQuantidade(item.getQuantidade() + quantidade);
            item.recalcularSubtotal();
        } else {
            var item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(produto.getPreco());
            item.recalcularSubtotal();
            pedido.getItens().add(item);
        }

        produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() - quantidade);
        produtoService.salvar(produto);
    }

    private void devolverAoEstoque(ItemPedido item) {
        var produto = item.getProduto();
        produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() + item.getQuantidade());
        produtoService.salvar(produto);
    }

    private void recalcularTotal(Pedido pedido) {
        var total = pedido.getItens().stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorTotal(total);
    }

    private Pedido buscarPedidoAtivo(Long id) {
        var optionalPedido = buscarPorId(id);
        if (!optionalPedido.isPresent())
            throw new IllegalArgumentException("Pedido não encontrado.");

        var pedido = optionalPedido.get();
        if (!pedido.isAtivo())
            throw new IllegalArgumentException("Pedido cancelado não pode ser alterado.");

        return pedido;
    }

    private ItemPedido buscarItem(Pedido pedido, Long itemId) {
        return pedido.getItens().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado no pedido."));
    }

    private String mensagemEstoque(Produto produto) {
        return "Estoque insuficiente para %s. Disponível: %d."
                .formatted(produto.getNome(), produto.getQuantidadeEmEstoque());
    }

}
