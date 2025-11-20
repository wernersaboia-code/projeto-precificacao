package com.projeto.precificacao.controller;

import com.projeto.precificacao.model.Produto;
import com.projeto.precificacao.repository.ProdutoRepository;
import com.projeto.precificacao.service.PrecificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    /**
     * GET /api/produtos - Retorna todos os produtos
     */
    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        return ResponseEntity.ok(produtoRepository.findAll());
    }

    /**
     * GET /api/produtos/{id} - Retorna um produto específico
     */
    @GetMapping("/{id}")
    public ResponseEntity<Produto> obterPorId(@PathVariable Long id) {
        return produtoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/produtos - Cria um novo produto
     */
    @PostMapping
    public ResponseEntity<Produto> criar(@RequestBody Produto produto) {
        try {
            // Calcula somatório de (Preço Compra * Quantidade) para todos os produtos
            List<Produto> todosProdutos = produtoRepository.findAll();
            BigDecimal somatorioCustosQuantidades = BigDecimal.ZERO;

            for (Produto p : todosProdutos) {
                BigDecimal custoTotal = p.getPrecoCusto().multiply(new BigDecimal(p.getQuantidadeEstimada()));
                somatorioCustosQuantidades = somatorioCustosQuantidades.add(custoTotal);
            }

            // Adiciona o novo produto ao somatório
            BigDecimal custoProdutoNovo = produto.getPrecoCusto().multiply(new BigDecimal(produto.getQuantidadeEstimada()));
            somatorioCustosQuantidades = somatorioCustosQuantidades.add(custoProdutoNovo);

            // Calcula custo fixo por unidade
            var custoFixoPorUnidade = PrecificacaoService.calcularCustoFixoPorUnidade(
                    produto.getCustoFixoMensal(),
                    produto.getPrecoCusto(),
                    somatorioCustosQuantidades
            );
            produto.setCustoFixoPorUnidade(custoFixoPorUnidade);

            // Calcula custo total base
            var custoTotalBase = PrecificacaoService.calcularCustoTotalBase(
                    produto.getPrecoCusto(),
                    custoFixoPorUnidade
            );
            produto.setCustoTotalBase(custoTotalBase);

            // Calcula o preço ideal usando custo total base
            var precoIdeal = PrecificacaoService.calcularPrecoIdeal(
                    custoTotalBase,
                    produto.getMargemDesejada(),
                    produto.getImpostosCustosVariaveis()
            );
            produto.setPrecoIdeal(precoIdeal);

            // Calcula lucro bruto por unidade = Preço Venda Ideal × Margem Desejada (%)
            var lucroBrutoPorUnidade = PrecificacaoService.calcularLucroBrutoPorUnidade(
                    precoIdeal,
                    produto.getMargemDesejada()
            );
            produto.setLucroBrutoPorUnidade(lucroBrutoPorUnidade);

            // Calcula margem bruta (preço ideal - preço compra)
            var margemBruta = PrecificacaoService.calcularMargemBruta(
                    precoIdeal,
                    produto.getPrecoCusto()
            );
            produto.setMargemBruta(margemBruta);

            // Calcula lucro mensal
            var lucroMensal = PrecificacaoService.calcularLucroMensal(
                    margemBruta,
                    produto.getQuantidadeEstimada()
            );
            produto.setLucroMensal(lucroMensal);

            // Calcula receita
            var receita = PrecificacaoService.calcularReceita(
                    precoIdeal,
                    produto.getQuantidadeEstimada()
            );
            produto.setReceita(receita);

            // Salva no banco
            Produto produtoSalvo = produtoRepository.save(produto);

            // Recalcula todos os outros produtos
            recalcularTodosProdutos();

            return ResponseEntity.ok(produtoSalvo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/produtos/{id} - Atualiza um produto
     */
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produtoAtualizado) {
        return produtoRepository.findById(id)
                .map(produto -> {
                    produto.setNome(produtoAtualizado.getNome());
                    produto.setPrecoCusto(produtoAtualizado.getPrecoCusto());
                    produto.setQuantidadeEstimada(produtoAtualizado.getQuantidadeEstimada());
                    produto.setCategoria(produtoAtualizado.getCategoria());
                    produto.setMargemDesejada(produtoAtualizado.getMargemDesejada());
                    produto.setImpostosCustosVariaveis(produtoAtualizado.getImpostosCustosVariaveis());
                    produto.setCustoFixoMensal(produtoAtualizado.getCustoFixoMensal());

                    produtoRepository.save(produto);
                    recalcularTodosProdutos();

                    return ResponseEntity.ok(produtoRepository.findById(id).get());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/produtos/{id} - Deleta um produto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (produtoRepository.existsById(id)) {
            produtoRepository.deleteById(id);
            recalcularTodosProdutos();
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Método auxiliar: recalcula todos os produtos quando há mudanças
     */
    private void recalcularTodosProdutos() {
        List<Produto> produtos = produtoRepository.findAll();

        if (produtos.isEmpty()) return;

        // Calcula somatório de (Preço Compra * Quantidade)
        BigDecimal somatorioCustosQuantidades = BigDecimal.ZERO;
        for (Produto p : produtos) {
            BigDecimal custoTotal = p.getPrecoCusto().multiply(new BigDecimal(p.getQuantidadeEstimada()));
            somatorioCustosQuantidades = somatorioCustosQuantidades.add(custoTotal);
        }

        // Recalcula cada produto
        for (Produto p : produtos) {
            // Custo fixo por unidade
            var custoFixoPorUnidade = PrecificacaoService.calcularCustoFixoPorUnidade(
                    p.getCustoFixoMensal(),
                    p.getPrecoCusto(),
                    somatorioCustosQuantidades
            );
            p.setCustoFixoPorUnidade(custoFixoPorUnidade);

            // Custo total base
            var custoTotalBase = PrecificacaoService.calcularCustoTotalBase(
                    p.getPrecoCusto(),
                    custoFixoPorUnidade
            );
            p.setCustoTotalBase(custoTotalBase);

            // Preço ideal
            var precoIdeal = PrecificacaoService.calcularPrecoIdeal(
                    custoTotalBase,
                    p.getMargemDesejada(),
                    p.getImpostosCustosVariaveis()
            );
            p.setPrecoIdeal(precoIdeal);

            // Lucro bruto por unidade
            var lucroBrutoPorUnidade = PrecificacaoService.calcularLucroBrutoPorUnidade(
                    precoIdeal,
                    p.getMargemDesejada()
            );
            p.setLucroBrutoPorUnidade(lucroBrutoPorUnidade);

            // Margem bruta
            var margemBruta = PrecificacaoService.calcularMargemBruta(
                    precoIdeal,
                    p.getPrecoCusto()
            );
            p.setMargemBruta(margemBruta);

            // Lucro mensal
            var lucroMensal = PrecificacaoService.calcularLucroMensal(
                    margemBruta,
                    p.getQuantidadeEstimada()
            );
            p.setLucroMensal(lucroMensal);

            // Receita
            var receita = PrecificacaoService.calcularReceita(
                    precoIdeal,
                    p.getQuantidadeEstimada()
            );
            p.setReceita(receita);

            produtoRepository.save(p);
        }
    }

    /**
     * GET /api/produtos/resumo/consolidado - Retorna resumo consolidado
     */
    @GetMapping("/resumo/consolidado")
    public ResponseEntity<Map<String, Object>> obterResumo() {
        try {
            List<Produto> produtos = produtoRepository.findAll();

            Map<String, Object> resultado = new HashMap<>();

            if (produtos.isEmpty()) {
                resultado.put("status", "Nenhum produto cadastrado");
                return ResponseEntity.ok(resultado);
            }

            // Usa o custo fixo do primeiro produto
            BigDecimal custoFixoMensal = produtos.get(0).getCustoFixoMensal();
            BigDecimal receitaTotalMensal = BigDecimal.ZERO;
            BigDecimal custoTotalCompra = BigDecimal.ZERO;
            BigDecimal lucroTotalPorUnidade = BigDecimal.ZERO;
            BigDecimal somatórioCustoVendaQuantidades = BigDecimal.ZERO;
            BigDecimal somatórioLucroBrutoQuantidades = BigDecimal.ZERO;
            Integer vendaEstimada = 0;

            for (Produto p : produtos) {
                if (p.getReceita() != null) {
                    receitaTotalMensal = receitaTotalMensal.add(p.getReceita());
                }

                BigDecimal custoProduto = p.getPrecoCusto().multiply(new BigDecimal(p.getQuantidadeEstimada()));
                custoTotalCompra = custoTotalCompra.add(custoProduto);

                // Lucro bruto total = SUMPRODUCT(Lucro Bruto por Unidade × Quantidade)
                if (p.getLucroBrutoPorUnidade() != null) {
                    BigDecimal lucroBrutoTotalProduto = p.getLucroBrutoPorUnidade().multiply(new BigDecimal(p.getQuantidadeEstimada()));
                    lucroTotalPorUnidade = lucroTotalPorUnidade.add(lucroBrutoTotalProduto);
                    somatórioLucroBrutoQuantidades = somatórioLucroBrutoQuantidades.add(lucroBrutoTotalProduto);
                }

                // Para cálculo de margem média percentual
                if (p.getPrecoIdeal() != null) {
                    somatórioCustoVendaQuantidades = somatórioCustoVendaQuantidades.add(
                            p.getPrecoIdeal().multiply(new BigDecimal(p.getQuantidadeEstimada()))
                    );
                }

                vendaEstimada += p.getQuantidadeEstimada();
            }

            // Margem Média em PERCENTUAL
            BigDecimal margemMediaPercentual = somatórioCustoVendaQuantidades.compareTo(BigDecimal.ZERO) > 0
                    ? somatórioLucroBrutoQuantidades.divide(somatórioCustoVendaQuantidades, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                    : BigDecimal.ZERO;

            // Ponto de Equilíbrio em R$ = Custo Fixo / Margem Média (%)
            BigDecimal pontoEquilibrioReais = margemMediaPercentual.compareTo(BigDecimal.ZERO) > 0
                    ? custoFixoMensal.divide(margemMediaPercentual.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Status: comparar quantidade total com ponto de equilíbrio
            BigDecimal margemMediaReais = vendaEstimada > 0 && somatórioLucroBrutoQuantidades.compareTo(BigDecimal.ZERO) > 0
                    ? somatórioLucroBrutoQuantidades.divide(new BigDecimal(vendaEstimada), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal pontoEquilibrioQuantidade = margemMediaReais.compareTo(BigDecimal.ZERO) > 0
                    ? custoFixoMensal.divide(margemMediaReais, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            String status = new BigDecimal(vendaEstimada).compareTo(pontoEquilibrioQuantidade) >= 0
                    ? "✅ Acima do ponto de equilíbrio!"
                    : "⚠️ Abaixo do ponto de equilíbrio!";

            // ROI Geral: Lucro Total / Custo Total Compra
            BigDecimal roiGeral = custoTotalCompra.compareTo(BigDecimal.ZERO) > 0
                    ? lucroTotalPorUnidade.divide(custoTotalCompra, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                    : BigDecimal.ZERO;

            resultado.put("custoFixoMensal", custoFixoMensal);
            resultado.put("receitaTotalMensal", receitaTotalMensal);
            resultado.put("custoTotalCompra", custoTotalCompra);
            resultado.put("margemContribuicaoTotal", lucroTotalPorUnidade);
            resultado.put("vendaEstimada", vendaEstimada);
            resultado.put("margemMediaPercentual", margemMediaPercentual);
            resultado.put("lucrobrutoTotal", lucroTotalPorUnidade);
            resultado.put("pontoEquilibrioReais", pontoEquilibrioReais);
            resultado.put("roiGeral", roiGeral);
            resultado.put("status", status);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao calcular resumo: " + e.getMessage());
            return ResponseEntity.badRequest().body(erro);
        }
    }
}
