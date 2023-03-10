# Версионность сайта

Scaladoc предоставляет удобный способ переключения между различными версиями документации. 
Эта функция полезна, если желательно предоставить старые документы пользователям, 
которые не перешли на новую версию библиотеки.

#### Как это настроить

Эта функция была разработана для легкой масштабируемости без необходимости 
повторного создания всех scaladocs после добавления новой версии. 
Для этого вводится новая настройка: `-versions-dictionary-url`. 
Его аргумент должен быть URL-адресом документа JSON, содержащего информацию о расположении конкретных версий. 
Файл JSON должен содержать свойство `versions` со словарём, 
связывающий метки определенных версий документации с URL-адресами, указывающими на их `index.html`.

Пример JSON-файла: 

```json
{
  "versions": {
    "3.0.x": "https://dotty.epfl.ch/3.0.x/docs/index.html",
    "Nightly": "https://dotty.epfl.ch/docs/index.html"
  }
}
```

Такие документы необходимо указывать для каждой из версий, однако позже это дает больше гибкости. 
Если необходимо добавить версию документов API рядом с предыдущими 5 версиями, которые уже опубликованы,
нужно только загрузить новые документы на веб-сервер и добавить новую запись в файл JSON. 
Все версии сайта теперь узнают о новой версии.

Важно отметить, что существует только один файл JSON, чтобы избежать избыточности, 
и каждый scaladoc должен заранее настроить свой URL-адрес, например, в sbt:

```text
doc / scalacOptions ++= Seq("-versions-dictionary-url", "https://dotty.epfl.ch/versions.json")
```

#### Как это выглядит с точки зрения пользователя

Предоставление файла JSON через `-versions-dictionary-url` позволяет scaladoc связывать версии. 
Также удобно иметь возможность изменить метку ревизии в выпадающем меню. 
Все изменится автоматически.

![](https://docs.scala-lang.org/resources/images/scala3/scaladoc/nightly.gif)


---

**Ссылки:**

- [Scaladoc Guide](https://docs.scala-lang.org/scala3/guides/scaladoc/site-versioning.html)
