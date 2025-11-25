# 💰 Sistema de Precificação Simples

Sistema inteligente para cálculo de preços com dashboard analytics em tempo real.

## 🚀 Sobre o Projeto

Sistema completo de precificação que ajuda empreendedores e pequenas empresas a calcular preços de venda de forma inteligente, considerando custos fixos, variáveis, margem de lucro e impostos.

## ✨ Funcionalidades

- **📦 Gestão de Produtos**: Cadastro completo de produtos com custos e categorias
- **🧮 Cálculos Automáticos**: Precificação automática baseada em margem desejada
- **📊 Dashboard Interativo**: Métricas financeiras em tempo real
- **📈 Analytics Avançados**: Gráficos e relatórios detalhados
- **📱 Design Responsivo**: Experiência otimizada para mobile e desktop
- **📤 Exportação de Dados**: Relatórios em CSV e TXT

## 🛠️ Tecnologias

### Frontend
- **HTML5** + **CSS3** + **JavaScript** Vanilla
- **Chart.js** para visualizações gráficas
- Design responsivo com CSS Grid/Flexbox
- Gradientes e animações CSS modernas

### Backend
- **Java Spring Boot**
- **Spring Data JPA**
- **PostgreSQL** (Supabase)
- API RESTful

### Deployment
- **Frontend**: Vercel
- **Backend**: Render
- **Database**: Supabase
- **Version Control**: GitHub

## 📦 Estrutura do Projeto
projeto-precificacao/
├── .github/
│ └── workflows/ # GitHub Actions
├── src/ # Código fonte Spring Boot
├── frontend/ # Arquivos estáticos (HTML, CSS, JS)
├── README.md # Este arquivo
└── pom.xml # Dependências Maven


## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.6+
- Node.js (opcional para desenvolvimento frontend)

### Backend (Spring Boot)
```bash
# Clone o repositório
git clone https://github.com/wernersaboia-code/projeto-precificacao.git

# Entre na pasta do projeto
cd projeto-precificacao

# Execute a aplicação
mvn spring-boot:run

Frontend
# O frontend está em /frontend ou como arquivos estáticos
# Pode ser servido por qualquer servidor web simples:

# Com Python
python -m http.server 8000

# Com Node.js
npx http-server

# Acesse: http://localhost:8000

🌐 URLs de Produção

    🌍 Aplicação: https://projeto-precificacao-knxy.vercel.app

    🔙 API: https://projeto-precificacao-ulbf.onrender.com

    🗄️ Database: Supabase

📊 Métricas de Cálculo

O sistema calcula automaticamente:

    Preço de venda ideal

    Margem de contribuição

    Custo fixo por unidade

    Ponto de equilíbrio

    ROI (Return on Investment)

    Lucro bruto mensal

🤝 Contribuição

Contribuições são sempre bem-vindas! Para contribuir:

    Fork o projeto

    Crie uma branch para sua feature (git checkout -b feature/AmazingFeature)

    Commit suas mudanças (git commit -m 'Add some AmazingFeature')

    Push para a branch (git push origin feature/AmazingFeature)

    Abra um Pull Request

📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.

👨‍💻 Autor

Werner Saboia

    GitHub: @wernersaboia-code

    LinkedIn: Werner Saboia

🎯 Próximas Funcionalidades

    Sistema de usuários e autenticação

    Histórico de preços

    Análise de concorrência

    Relatórios personalizados

    Integração com marketplaces