UDPDrawingTool
====

## 概要
Javaで実装したお絵描き通信ツール．

UDP通信により，1対1のお絵描き共有を可能にしている．

描いた絵は画像として保存することが可能となっている．




## 使用方法
1. jarファイルから起動する場合
  1. ユーザ1を起動する

    ```sh
    $ java -jar Graphicstest.jar localhost 50000 50001
    ```

  2. ユーザ2を起動する

    ```sh
    $ java -jar Graphicstest.jar localhost 50001 50000
    ```

2. classファイルから起動する場合
  1. ユーザ1を起動する

    ```sh
    $ java -classpath bin GraphicsTest localhost 50000 50001
    ```

  2. ユーザ2を起動する

    ```sh
    $ java -classpath bin GraphicsTest localhost 50001 50000
    ```




## ビルド手順
### Antを用いる場合
以下のように，トップディレクトリにあるbuild.xmlを用いるとよい(要:antコマンド)．

```sh
$ ant
```


### GNU Makeを用いる場合
以下のように，トップディレクトリにあるMakefileを用いるとよい(要:makeコマンド)．

```sh
$ make
```


### 手動でビルドする場合
以下のコマンドでコンパイルすることができる．

```sh
$ javac src/GraphicsTest.java -d bin -encoding utf-8
```

Jarファイルの作成方法については，割愛する．




## Javadocの生成
付属のbuild.xmlとMakefileを用いると，簡単にJavadocを生成することができる．


### Antを用いる場合
以下のように，antコマンドの引数に"javadoc"を与えると，javadocディレクトリにドキュメントが生成される．

```sh
$ ant javadoc
```


### Gnu Makeを用いる場合
以下のように，makeコマンドの引数に"javadoc"を与えると，javadocディレクトリにドキュメントが生成される．

```sh
$ make javadoc
```


## LICENSE

This software is released under the MIT License, see [LICENSE](LICENSE).
