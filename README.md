# RefactorHub

![logo](/client/src/static/logo.png)

RefactorHub is a commit annotator for refactoring.

## Demonstration

[![demo](https://img.youtube.com/vi/-aoREGs5zaI/sddefault.jpg)](https://www.youtube.com/watch?v=-aoREGs5zaI)

## How to Run

1. Create OAuth App [here](https://github.com/settings/developers) with `http://localhost:8080` as callback URL, and get `CLIENT_ID` and `CLIENT_SECRET`.
2. Generate access token [here](https://github.com/settings/tokens), and get `GITHUB_ACCESS_TOKEN`.
3. Set `CLIENT_ID`, `CLIENT_SECRET`, and `GITHUB_ACCESS_TOKEN` on `.env` file.
4. `docker-compose up`
5. Open http://localhost:8080

## Deploy

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/salab/RefactorHub)

## How to Use

### Add Refactoring Type

After sign in, open "Refactoring Types" page.
And create a new RefactoringType in the following JSON format.

<details>
<summary>RefactoringType JSON format</summary>

```json
{
  "name": "Extract Method",
  "description": "Extracting code fragments from existing method, and creating a new method based on the extracted code",
  "url": "https://refactoring.com/catalog/extractFunction.html",
  "before": {
    "extracted code": {
      "description": "Code fragments which is extracted",
      "type": "CodeFragment",
      "multiple": true,
      "required": true
    },
    "target method": {
      "description": "Method in which the extraction is performed",
      "type": "MethodDeclaration",
      "autofills": [
        {
          "type": "Surround",
          "follows": [
            {
              "category": "before",
              "name": "extracted code"
            }
          ],
          "element": "MethodDeclaration"
        }
      ]
    }
  },
  "after": {
    "invocation": {
      "description": "Method invocation by which extracted code was replaced",
      "type": "MethodInvocation",
      "required": true
    },
    "extracted code": {
      "description": "Code fragments which was extracted",
      "type": "CodeFragment",
      "multiple": true,
      "required": true
    },
    "extracted method": {
      "description": "Method which was newly created by the extraction",
      "type": "MethodDeclaration",
      "autofills": [
        {
          "type": "Surround",
          "follows": [
            {
              "category": "after",
              "name": "extracted code"
            }
          ],
          "element": "MethodDeclaration"
        }
      ]
    },
    "target method": {
      "description": "Method in which the extraction was performed",
      "type": "MethodDeclaration",
      "autofills": [
        {
          "type": "Surround",
          "follows": [
            {
              "category": "after",
              "name": "invocation"
            }
          ],
          "element": "MethodDeclaration"
        }
      ]
    }
  }
}
```

</details>

### Add Experiment

After sign in, click "Experiment" button.
And create a new Experiment with `title`, `description` and `refactorings` which is the following JSON format.

<details>
<summary>refactorings NDJSON format</summary>

```json
{"type":"Extract Method","commit":{"sha":"cb49e436b9d7ee55f2531ebc2ef1863f5c9ba9fe","owner":"rstudio","repository":"rstudio"},"data":{"before":{},"after":{}},"description":"Extract Method protected setMaxHeight(maxHeight int) : void extracted from protected wrapMenuBar(menuBar ToolbarMenuBar) : Widget in class org.rstudio.core.client.widget.ScrollableToolbarPopupMenu"}
{"type":"Move Attribute","commit":{"sha":"f05e86c4d31987ff2f30330745c3eb605de4c4dc","owner":"Graylog2","repository":"graylog2-server"},"data":{"before":{},"after":{}},"description":"Move Attribute private COMPARATOR : Comparator<IndexRange> from class org.graylog2.indexer.ranges.MongoIndexRangeService to public COMPARATOR : Comparator<IndexRange> from class org.graylog2.indexer.ranges.IndexRange"}
{"type":"Any","commit":{"sha":"08f37df9f39f101bba0ee96845e232d2c72bf426","owner":"JetBrains","repository":"intellij-community"},"data":{"before":{},"after":{}},"description":""}
{"type":"Extract Method","commit":{"sha":"6cf596df183b3c3a38ed5dd9bb3b0100c6548ebb","owner":"realm","repository":"realm-java"},"data":{"before":{"target method":{"type":"MethodDeclaration","multiple":false,"elements":[{"name":"showStatus","className":"io.realm.examples.realmmigrationexample.MigrationExampleActivity","location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":112,"startColumn":5,"endLine":118,"endColumn":6}},"type":"MethodDeclaration"}]},"extracted code":{"type":"CodeFragment","multiple":true,"elements":[{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":114,"startColumn":9,"endLine":114,"endColumn":25}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":115,"startColumn":9,"endLine":115,"endColumn":42}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":116,"startColumn":9,"endLine":116,"endColumn":25}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":117,"startColumn":9,"endLine":117,"endColumn":32}},"type":"CodeFragment"}]}},"after":{"target method":{"type":"MethodDeclaration","multiple":false,"elements":[{"name":"showStatus","className":"io.realm.examples.realmmigrationexample.MigrationExampleActivity","location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":127,"startColumn":5,"endLine":129,"endColumn":6}},"type":"MethodDeclaration"}]},"extracted method":{"type":"MethodDeclaration","multiple":false,"elements":[{"name":"showStatus","className":"io.realm.examples.realmmigrationexample.MigrationExampleActivity","location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":131,"startColumn":5,"endLine":136,"endColumn":6}},"type":"MethodDeclaration"}]},"invocation":{"type":"MethodInvocation","multiple":false,"elements":[{"methodName":"showStatus","className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":128,"startColumn":9,"endLine":128,"endColumn":39}},"type":"MethodInvocation"}]},"extracted code":{"type":"CodeFragment","multiple":true,"elements":[{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":132,"startColumn":9,"endLine":132,"endColumn":25}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":133,"startColumn":9,"endLine":133,"endColumn":42}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":134,"startColumn":9,"endLine":134,"endColumn":25}},"type":"CodeFragment"},{"methodName":null,"className":null,"location":{"path":"examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/MigrationExampleActivity.java","range":{"startLine":135,"startColumn":9,"endLine":135,"endColumn":32}},"type":"CodeFragment"}]}}},"description":"Extract Method\tprivate showStatus(txt String) : void extracted from private showStatus(realm Realm) : void in class io.realm.examples.realmmigrationexample.MigrationExampleActivity"}
{"type":"Move Attribute","commit":{"sha":"6abc40ed4850d74ee6c155f5a28f8b34881a0284","owner":"BuildCraft","repository":"BuildCraft"},"data":{"before":{"target field":{"type":"FieldDeclaration","multiple":false,"elements":[{"name":"BUTTON_TEXTURES","className":"buildcraft.core.lib.gui.buttons.GuiBetterButton","location":{"path":"common/buildcraft/core/lib/gui/buttons/GuiBetterButton.java","range":{"startLine":26,"startColumn":39,"endLine":26,"endColumn":120}},"type":"FieldDeclaration"}]}},"after":{"moved field":{"type":"FieldDeclaration","multiple":false,"elements":[{"name":"BUTTON_TEXTURES","className":"buildcraft.core.lib.gui.buttons.StandardButtonTextureSets","location":{"path":"common/buildcraft/core/lib/gui/buttons/StandardButtonTextureSets.java","range":{"startLine":18,"startColumn":39,"endLine":18,"endColumn":120}},"type":"FieldDeclaration"}]}}},"description":"Move Attribute\tpublic BUTTON_TEXTURES : ResourceLocation from class buildcraft.core.lib.gui.buttons.GuiBetterButton to public BUTTON_TEXTURES : ResourceLocation from class buildcraft.core.lib.gui.buttons.StandardButtonTextureSets"}
```

</details>

### Annotate refactorings in Experiment

Open the new created Experiment, and annotate refactorings.

### Get all result as JSON

After annotation, click "Get All Result" button on the Experiment page.

## Supported Refactoring Types

<details>
<summary>RefactorHub supports the following 25 refactoring types:</summary>

* Extract Method
* Move Attribute
* Move Class
* Rename Variable
* Inline Method
* Extract Interface
* Push Down Method
* Push Down Attribute
* Pull Up Method
* Pull Up Attribute
* Move Method
* Extract And Move Method
* Rename Method
* Extract Superclass
* Rename Parameter
* Rename Class
* Move And Rename Class
* Parameterize Variable
* Move And Inline Method
* Move And Rename Method
* Extract Variable
* Change Return Type
* Rename Attribute
* Change Parameter Type
* Change Variable Type

</details>
