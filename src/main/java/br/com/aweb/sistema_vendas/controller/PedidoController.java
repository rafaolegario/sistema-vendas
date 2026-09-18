package br.com.aweb.sistema_vendas.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.aweb.sistema_vendas.model.ItemPedido;
import br.com.aweb.sistema_vendas.model.Pedido;
import br.com.aweb.sistema_vendas.service.ClienteService;
import br.com.aweb.sistema_vendas.service.PedidoService;
import br.com.aweb.sistema_vendas.service.ProdutoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    // Listar pedidos
    @GetMapping
    public ModelAndView list() {
        return new ModelAndView("pedido/list", Map.of("pedidos", pedidoService.listarTodos()));
    }

    // Formulário de cadastro
    @GetMapping("/novo")
    public ModelAndView create() {
        var pedido = new Pedido();
        pedido.getItens().add(new ItemPedido());
        return formView(pedido);
    }

    // Salvar pedido
    @PostMapping("/novo")
    public Object create(@Valid Pedido pedido, BindingResult result) {
        if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
            result.rejectValue("cliente", "cliente.obrigatorio", "Selecione um cliente.");
        }

        // Linhas que o usuário adicionou e deixou sem produto
        pedido.getItens().removeIf(
                item -> item == null || item.getProduto() == null || item.getProduto().getId() == null);
        if (pedido.getItens().isEmpty()) {
            result.reject("itens.obrigatorio", "Adicione pelo menos um produto ao pedido.");
        }

        if (result.hasErrors()) {
            return formView(pedido);
        }

        try {
            pedidoService.salvar(pedido);
        } catch (IllegalArgumentException e) {
            result.reject("pedido.invalido", e.getMessage());
            return formView(pedido);
        }

        return "redirect:/pedidos";
    }

    // Tela de alteração do pedido
    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable Long id) {
        var optionalPedido = pedidoService.buscarPorId(id);
        if (optionalPedido.isPresent()) {
            return new ModelAndView("pedido/edit", Map.of(
                    "pedido", optionalPedido.get(),
                    "produtos", produtoService.listarTodos()));
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    // Adicionar produto ao pedido
    @PostMapping("/edit/{id}/item/novo")
    public String adicionarProduto(@PathVariable Long id, @RequestParam Long produtoId,
            @RequestParam Integer quantidade, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.adicionarProduto(id, produtoId, quantidade);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/edit/" + id;
    }

    // Alterar a quantidade de um produto do pedido
    @PostMapping("/edit/{id}/item/{itemId}")
    public String alterarQuantidade(@PathVariable Long id, @PathVariable Long itemId,
            @RequestParam Integer quantidade, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.alterarQuantidade(id, itemId, quantidade);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/edit/" + id;
    }

    // Remover produto do pedido
    @PostMapping("/edit/{id}/item/{itemId}/remover")
    public String removerProduto(@PathVariable Long id, @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoService.removerProduto(id, itemId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos/edit/" + id;
    }

    // Cancelar pedido
    @GetMapping("/cancel/{id}")
    public ModelAndView cancel(@PathVariable Long id) {
        var optionalPedido = pedidoService.buscarPorId(id);
        if (optionalPedido.isPresent()) {
            return new ModelAndView("pedido/cancel", Map.of("pedido", optionalPedido.get()));
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cancelar(id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pedidos";
    }

    // O formulário precisa das listas de clientes e produtos também quando volta com erro
    private ModelAndView formView(Pedido pedido) {
        // Garante ao menos uma linha de produto na tela
        if (pedido.getItens().isEmpty()) {
            pedido.getItens().add(new ItemPedido());
        }
        return new ModelAndView("pedido/form", Map.of(
                "pedido", pedido,
                "clientes", clienteService.listarTodos(),
                "produtos", produtoService.listarTodos()));
    }

}
