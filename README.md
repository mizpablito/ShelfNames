# ShelfNames

Lekki plugin do Minecraft (Paper), który wyświetla **nazwy itemów znajdujących się na półkach** (`*_SHELF`) w formie **hologramu nad blokiem**, gdy gracz na niego patrzy.

Plugin został zaprojektowany z naciskiem na **wydajność**, **brak zbędnych alokacji** oraz **minimalny wpływ na server thread**.

---

## ✨ Funkcje

- Wyświetlanie nazw itemów z półki jako hologram
- Obsługa wszystkich wariantów drewnianych półek (`Tag.WOODEN_SHELVES`)
- Zachowanie kolorów i formatowania nazw itemów
- Aktualizacja hologramu tylko przy realnej zmianie zawartości
- Automatyczne usuwanie hologramu po odejściu wzroku
- Brak migotania i zbędnych aktualizacji
- W pełni kompatybilny z Adventure / MiniMessage

---

## ⚙️ Jak działa

- Co określoną liczbę ticków plugin sprawdza, **na jaki blok patrzy gracz**
- Jeśli jest to półka:
    - porównywana jest jej pozycja z poprzednią (cache)
    - snapshot zawartości tworzony jest **tylko przy zmianie oglądanej półki**
- Hologram aktualizowany jest **tylko** po zmianie oglądanej półki 
- Kosztowne operacje (`BlockState`) wykonywane są **wyłącznie wtedy, gdy są potrzebne**

---

## 🔧 Konfiguracja

```yaml
update-interval-ticks: 20
max-distance: 5
only-custom-names: true

holograms:
  provider: FANCY
  offset-y: 1.2
```

- `update-interval-ticks` - co jaki czas wykonywać pętle sprawdzania i odświeżania hologramów (domyślnie 20 ticks)
- `max-distance` - maksymalna odległość sprawdzania bloków na które patrzy gracz
- `only-custom-names` - wyświetlaj tylko itemy z customową nazwąość sprawdzania wzroku
- `hologram.provieder` - plugin obsługujący hologramy, aktualnie FancyHolograms
- `hologram.offset-y` - wysokość hologramu nad półką

---

## 📦 Wymagania

- Paper 1.21+
- FancyHolograms 2.8.0+
- Java 21

##🧩 Zależności

- [FancyHolograms](https://modrinth.com/plugin/fancyholograms)
- Paper API
- Adventure (wbudowane w Paper)

## 🚀 Planowane funkcje

- Dodanie wsparcia dla [DecentHolograms](https://www.spigotmc.org/resources/decentholograms-1-8-1-21-11-papi-support-no-dependencies.96927/)
- Opcjonalne wygładzanie przejść (fade in / fade out)

## 🧠 Uwagi techniczne

Plugin nie używa NMS, nie wysyła własnych pakietów i nie ingeruje w tick loop serwera.

Został zoptymalizowany przy użyciu Spark Profiler i testowany pod kątem realnego obciążenia.