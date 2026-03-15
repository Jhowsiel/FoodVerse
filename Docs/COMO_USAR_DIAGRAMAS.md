# Como usar os diagramas UML

Os arquivos `.puml` nesta pasta contêm o código-fonte dos diagramas UML do FoodVerse.
Para visualizá-los, use um dos métodos abaixo.

---

## Arquivos disponíveis

| Arquivo | Diagrama |
|---------|----------|
| `DiagramaCasoDeUso.puml` | Diagrama de Caso de Uso |
| `DiagramaDeClasse.puml` | Diagrama de Classe |

---

## Opção 1 — Site plantuml.com (mais fácil)

1. Abra [https://www.plantuml.com/plantuml/uml/](https://www.plantuml.com/plantuml/uml/)
2. Apague o conteúdo padrão da caixa de texto
3. Cole o conteúdo do arquivo `.puml` desejado
4. O diagrama é gerado automaticamente à direita
5. Para salvar: clique com o botão direito na imagem → "Salvar imagem como…"

---

## Opção 2 — VS Code com extensão

1. Instale a extensão **PlantUML** no VS Code (`jebbs.plantuml`)
2. Instale o Java (necessário para a extensão funcionar localmente)
3. Abra o arquivo `.puml`
4. Pressione `Alt + D` para visualizar o diagrama
5. Clique com o botão direito no editor → **"Export Current Diagram"** para exportar como PNG ou SVG

---

## Opção 3 — IntelliJ IDEA / outras IDEs

A maioria das IDEs JetBrains (IntelliJ, PyCharm) tem plugin oficial de PlantUML.
Instale via `File → Settings → Plugins → Marketplace → PlantUML Integration`.

---

## Opção 4 — draw.io (diagrams.net)

O draw.io suporta importação de PlantUML via menu:
`Extras → Edit Diagram → Cole o código` (pode exigir plugin ativo)

---

## Dica

Se o diagrama ficar muito grande para caber na tela, no plantuml.com é possível
aumentar o zoom clicando diretamente na imagem gerada.
