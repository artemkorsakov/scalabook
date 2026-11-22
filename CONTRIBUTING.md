# Contributing Guide

## English

Thank you for wanting to help the project! Any help is welcome — from reporting a bug to submitting code.

### How to help

1. **Report a bug** — open an Issue with the `bug` label, describe the problem and steps to reproduce it.
2. **Suggest an idea** — open an Issue with the `enhancement` label and describe in detail what you want to add and why.
3. **Write code** — look for Issues tagged `good first issue` (for beginners) or `help wanted`.
4. **Improve the documentation** — typos, examples, clarifications — all of it matters.

### Code contribution process

1. **Fork** the repository on GitHub or GitVerse.
2. **Clone** your copy locally:
   GitHub:
   ```bash
   git clone https://gitverse.ru/artemkorsakov/scalabook.git
   ```
   or GitVerse:
   ```bash
   git clone https://gitverse.ru/artemkorsakov/scalabook.git
   ```
3. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/short-feature-name
   ```
   or for a bug fix:
   ```bash
   git checkout -b fix/bug-description
   ```
4. **Make your changes**. Prefer small, logically complete commits.
5. **Run the tests**:
   ```bash
   sbt prePush
   ```
6. **Write a clear commit message**:
    - Good: `Add email input validation`
    - Bad: `fix`
7. **Push the branch** to your fork:
   ```bash
   git push origin feature/short-feature-name
   ```
8. **Open a Pull Request (PR)** against the `main` branch of the original repository.

### Pull Request requirements

- In the PR description, state **what** you changed and **why**.
- If the PR closes an Issue, write `Closes #issue_number`.
- Make sure all automated checks (CI) pass.
- Be ready for discussion and revisions — that's normal.

### Code style

- Follow the style already used in the project.
- Use meaningful names for variables and functions.
- Avoid deep nesting and code duplication.
- Comment complex or non-obvious parts.

### Adding new dependencies

Try not to add new dependencies unless necessary. If they are really needed, justify them in the PR.

### Questions?

If you are not sure how best to implement a feature, or want to discuss an idea before starting work — open an Issue
with the `discussion` label.

Thanks again for your contribution! 💙

---

# Руководство по участию в проекте

## Русский

Спасибо, что хотите помочь проекту! Любая помощь приветствуется — от сообщения об ошибке до отправки кода.

## Как помочь

1. **Сообщить об ошибке** — создайте Issue с пометкой `bug`, опишите проблему и шаги для воспроизведения.
2. **Предложить идею** — создайте Issue с пометкой `enhancement` и подробно опишите, что вы хотите добавить и зачем.
3. **Написать код** — найдите Issue с метками `good first issue` (для новичков) или `help wanted`.
4. **Улучшить документацию** — опечатки, примеры, уточнения — всё это важно.

## Процесс работы над кодом

1. **Форкните** репозиторий на GitHub или GitVerse.
2. **Склонируйте** свою копию локально:
   GitHub:
   ```bash
   git clone https://gitverse.ru/artemkorsakov/scalabook.git
   ```
   или GitVerse:
   ```bash
   git clone https://gitverse.ru/artemkorsakov/scalabook.git
   ```
3. **Создайте ветку** для ваших изменений:
   ```bash
   git checkout -b feature/короткое-название-фичи
   ```
   или для исправления ошибки:
   ```bash
   git checkout -b fix/описание-бага
   ```
4. **Внесите изменения**. Старайтесь делать небольшие, логически завершённые коммиты.
5. **Запустите тесты**:
   ```bash
   sbt prePush
   ```
6. **Напишите понятное сообщение коммита**:
    - Хорошо: `Добавлена проверка ввода email`
    - Плохо: `фикс`
7. **Запушьте ветку** в свой форк:
   ```bash
   git push origin feature/короткое-название-фичи
   ```
8. **Откройте Pull Request (PR)** в ветку `main` исходного репозитория.

## Требования к Pull Request

- В описании PR укажите, **что** вы изменили и **почему**.
- Если PR закрывает какой-то Issue, напишите `Closes #номер_issue`.
- Убедитесь, что все автоматические проверки (CI) проходят успешно.
- Будьте готовы к обсуждению и правкам — это нормально.

## Стиль кода

- Следуйте стилю, который уже используется в проекте.
- Используйте осмысленные имена переменных и функций.
- Избегайте глубокой вложенности и дублирования кода.
- Комментируйте сложные или неочевидные места.

## Добавление новых зависимостей

Старайтесь не добавлять новых зависимостей без необходимости. Если они действительно нужны, обоснуйте это в PR.

## Вопросы?

Если вы не уверены, как лучше реализовать фичу, или хотите обсудить идею до начала работы — создайте Issue 
с пометкой `discussion`.

Ещё раз спасибо за ваш вклад! 💙