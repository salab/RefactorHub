# RefactorHub

![logo](/client/src/public/logo.png)

RefactorHub is a commit annotator for refactoring.

## How to Run

1. Create OAuth App [here](https://github.com/settings/developers) with `http://localhost:8080` as callback URL, and get `CLIENT_ID` and `CLIENT_SECRET`.
2. Generate access token [here](https://github.com/settings/tokens), and get `GITHUB_ACCESS_TOKEN`.
3. Set `CLIENT_ID`, `CLIENT_SECRET`, and `GITHUB_ACCESS_TOKEN` on `.env` file.
4. `docker-compose up`
5. Open http://localhost:8080

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
  "referenceUrl": "https://refactoring.com/catalog/extractFunction.html",
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
And create a new Experiment with `title`, `description` and `commits` which is the following JSON format.

<details>
<summary>commits NDJSON format</summary>

```json lines
{"sha":"cb49e436b9d7ee55f2531ebc2ef1863f5c9ba9fe","owner":"rstudio","repository":"rstudio"}
{"sha":"f05e86c4d31987ff2f30330745c3eb605de4c4dc","owner":"Graylog2","repository":"graylog2-server"}
{"sha":"3972b9b3d4e03bdb5e62dfa663e3e0a1871e3c9f","owner":"JetBrains","repository":"intellij-community"}
{"sha":"726dd024720727bd2db08af31deab693e22a6173","owner":"bitcoinj","repository":"bitcoinj"}
{"sha":"08f37df9f39f101bba0ee96845e232d2c72bf426","owner":"JetBrains","repository":"intellij-community"}
```

</details>

### Annotate refactorings in Experiment

Open the new created Experiment, and annotate refactorings.

### Get all result as JSON

After annotation, click "RESULT" button on the Experiment page.

## Supported Refactoring Types

<details>
<summary>RefactorHub supports the following 37 refactoring types:</summary>

* Extract Method
* Move Method
* Rename Method
* Inline Method
* Push Down Method
* Pull Up Method
* Extract And Move Method
* Move And Inline Method
* Move And Rename Method
* Extract Attribute
* Move Attribute
* Rename Attribute
* Inline Attribute
* Push Down Attribute
* Pull Up Attribute
* Extract Variable
* Rename Variable
* Inline Variable
* Parameterize Variable
* Rename Parameter
* Move Class
* Rename Class
* Move And Rename Class
* Extract Superclass
* Extract Interface
* Trim
* Trim-INVERSE
* Change Conditional to Switch
* Reverse Conditional
* Restructure Conditional Expression
* Introduce Block
* Expand Block
* Change Method Access Modifier
* Change Attribute Access Modifier
* Change Return Type
* Change Parameter Type
* Change Variable Type

</details>

## Related Publication
If you use or mention this tool in a scientific publication, we would appreciate citations to the following paper:

Ryo Kuramoto, Motoshi Saeki, Shinpei Hayashi: "RefactorHub: A Commit Annotator for Refactoring". In Proceedings of the 29th IEEE/ACM International Conference on Program Comprehension (ICPC 2021), pp. 495-499, 2021. DOI: https://doi.org/10.1109/ICPC52881.2021.00058 / Preprint: https://arxiv.org/abs/2103.11563
```
@inproceedings{kuramoto-icpc2021,
    author = {Ryo Kuramoto and Motoshi Saeki and Shinpei Hayashi},
    title = {{RefactorHub}: A Commit Annotator for Refactoring},
    booktitle = {Proceedings of the 29th IEEE/ACM International Conference on Program Comprehension (ICPC 2021)},
    pages = {495--499},
    year = 2021,
    doi = {10.1109/ICPC52881.2021.00058}
}
```
