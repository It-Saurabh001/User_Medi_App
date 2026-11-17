# GreenShield Pharmacy: Color Guide (Blue Logo Version)

Ye guide naye **blue logo** ke liye hai. Structure, niyam aur touch behavior pehle wale green guide jaise hi hain. Bas brand ka rang green se blue ho gaya hai.

App ka main rang ab **Blue 600 (`#0A6EE0`)** hai. Ye logo ke shield, capsule aur cross ke blue se liya hai, thoda gehra rakha gaya hai taaki white text saaf padhe (contrast 4.86 : 1).

**Kul colors: 44** (41 app ke liye + 3 sirf logo ke liye)

| Family | Kitne shades | Kaam |
|---|---|---|
| Blue | 10 | Main brand, buttons, selected, links |
| Green | 6 | Sirf success: In stock, Available, Completed |
| Amber (warm) | 7 | Warning: low stock, near expiry |
| Red (warm) | 6 | Danger: out of stock, expired, delete |
| Sky (cool) | 6 | Info: batches, movements, tags |
| Neutral | 6 | Text, background, borders |
| Logo only | 3 | Sirf logo/splash, UI mein nahi |

### Pehle guide se kya badla

- **Blue** ab brand family hai (pehle Green thi).
- **Green abhi bhi hai, par sirf success ke liye.** Logo ab poora blue hai, isliye "In stock" jaisi cheezein blue hoti to brand aur status mix ho jaate. Green ka matlab "theek hai" hi rahe.
- **Teal ki jagah Sky** (logo ka halka cyan-blue) info ke liye aaya hai.
- **Neutrals** pehle hare-bhure gray the. Ab ye halke blue-tinted hain, taaki blue ke saath mel khayein.
- **Amber aur Red bilkul wahi hain.** Unke hex aur touch shades nahi badle.

---

## 1. Golden rule: touch par same family ka shade

Kisi cheez ka rang jis family ka hai, touch par uska rang usi family ka rahega. Rang kabhi family badal kar nahi jayega.

- Blue cheez dabao → blue ka doosra shade.
- Green (success) cheez dabao → green ka doosra shade.
- Amber (warm) cheez dabao → amber ka doosra shade.
- Red cheez dabao → red ka doosra shade.
- Sky cheez dabao → sky ka doosra shade.

**Shade kitna badle (2 niyam):**

| Cheez ka background | Press par | Long press / hold par |
|---|---|---|
| **Halka** (50, 100: card, row, chip, tile) | Ek step gehra (50 → 100, 100 → 200) | Do step gehra (50 → 200, 100 → 300) |
| **Gehra** (600: solid button) | Ek step gehra (600 → 700) | Do step gehra (600 → 800) |

Press aur hold ke beech fark hamesha dikhna chahiye. Agar dono ek jaise dikhein, to hold wala shade ek aur gehra karo.

---

## 2. Blue family (10 shades) — Brand aur actions

| Naam | Hex | Kab use karna hai |
|---|---|---|
| Blue 50 | `#EEF5FF` | Poori screen ka background. Tappable row ka press shade |
| Blue 100 | `#DCEBFF` | Selected chip, selected nav item, icon tile |
| Blue 200 | `#B8D6FD` | Border, progress bar ka khali hissa, chip ka press shade |
| Blue 300 | `#86BAFB` | Disabled button, chip ka hold shade |
| Blue 400 | `#4D9AF5` | Gradient ka halka sira, chhote decoration |
| Blue 500 | `#1F82EC` | Focus border (TextField), links ka halka accent |
| **Blue 600** | **`#0A6EE0`** | **Primary rang: buttons, active icon, selected text, links** |
| Blue 700 | `#0858B8` | Primary button dabane par, status bar |
| Blue 800 | `#064490` | Primary button hold par, gehra gradient |
| Blue 900 | `#042F66` | Sabse gehra blue text (kam use) |

### Blue cheezon ka touch behavior

| Cheez | Normal | Press | Long press / hold |
|---|---|---|---|
| Primary button (Add product, Save) | 600 `#0A6EE0` | 700 `#0858B8` | 800 `#064490` |
| Text link ("View all") | 600 `#0A6EE0` | 700 `#0858B8` | 800 `#064490` |
| Selected chip | 100 `#DCEBFF` + border 600 | 200 `#B8D6FD` | 300 `#86BAFB` |
| Selected bottom-nav item | 100 `#DCEBFF` | 200 `#B8D6FD` | 300 `#86BAFB` |
| Secondary button (white + blue border) | White | 100 `#DCEBFF` | 200 `#B8D6FD` |
| Product row / list row | White | 50 `#EEF5FF` | 100 `#DCEBFF` |
| Tappable card | White | 50 `#EEF5FF` | 100 `#DCEBFF` |
| Icon button (bell, scan, back) | `#F3F7FD` | 100 `#DCEBFF` | 200 `#B8D6FD` |
| Unselected chip | White | 50 `#EEF5FF` | 100 `#DCEBFF` |

---

## 3. Green family (6 shades) — Sirf success

Green ko brand ya button ke liye mat use karo. Ye sirf "sab theek hai" dikhane ke liye hai.

| Naam | Hex | Kab use karna hai |
|---|---|---|
| Green 50 | `#EFF9F4` | Success card ka halka background, row press shade |
| Green 100 | `#D9F2E4` | Success pill ka background (In stock, Available, Done) |
| Green 200 | `#B3E5C9` | Success pill ka press shade |
| **Green 600** | **`#12804A`** | Success text, icon, "+12.8%" jaise badhat ke numbers |
| Green 700 | `#0E6439` | Success text/icon dabane par |
| Green 800 | `#0A4C2B` | Success text/icon hold par |

### Green cheezon ka touch behavior

| Cheez | Normal | Press | Long press / hold |
|---|---|---|---|
| Success pill / chip (tappable) | bg 100 `#D9F2E4`, text 600 | bg 200 `#B3E5C9`, text 700 | bg 200 `#B3E5C9`, text 800 |
| Success card (e.g. "Order completed") | bg 50 `#EFF9F4` | bg 100 `#D9F2E4` | bg 200 `#B3E5C9` |
| Success text link | 600 `#12804A` | 700 `#0E6439` | 800 `#0A4C2B` |

---

## 4. Amber family (7 shades) — Warm: warning

| Naam | Hex | Kab use karna hai |
|---|---|---|
| Amber 50 | `#FFF8EB` | Warning card ka halka background, row press shade |
| Amber 100 | `#FFEBC8` | Warning pill ka background (Low stock, Near expiry) |
| Amber 200 | `#FFD791` | Warning pill ka press shade, border |
| Amber 400 | `#E8A23A` | Warning icon, chart ka warning bar |
| **Amber 600** | **`#B8730A`** | Warning ka text aur icon |
| Amber 700 | `#965C08` | Warning button ya text dabane par |
| Amber 800 | `#754806` | Warning button ya text hold par |

### Amber cheezon ka touch behavior

| Cheez | Normal | Press | Long press / hold |
|---|---|---|---|
| Warning pill / chip (tappable) | bg 100 `#FFEBC8`, text 600 | bg 200 `#FFD791`, text 700 | bg 400 `#E8A23A`, text 800 |
| Warning card (e.g. "12 low stock") | bg 50 `#FFF8EB` | bg 100 `#FFEBC8` | bg 200 `#FFD791` |
| Warning alert row | White | 50 `#FFF8EB` | 100 `#FFEBC8` |
| Warning text link ("Restock now") | 600 `#B8730A` | 700 `#965C08` | 800 `#754806` |
| Warning solid button ("Review stock") | 600 `#B8730A` | 700 `#965C08` | 800 `#754806` (text white) |

---

## 5. Red family (6 shades) — Warm: danger

| Naam | Hex | Kab use karna hai |
|---|---|---|
| Red 50 | `#FFF1F2` | Error card ka halka background, row press shade |
| Red 100 | `#FFDDE0` | Danger pill ka background (Out of stock, Expired) |
| Red 200 | `#FFBFC5` | Danger pill press shade, error border |
| **Red 600** | **`#D13440`** | Danger text, icon, delete button, error message |
| Red 700 | `#B52A35` | Danger button dabane par |
| Red 800 | `#8F1F29` | Danger button hold par |

### Red cheezon ka touch behavior

| Cheez | Normal | Press | Long press / hold |
|---|---|---|---|
| Danger button (Delete, Write-off) | 600 `#D13440` | 700 `#B52A35` | 800 `#8F1F29` |
| Danger pill / chip (tappable) | bg 100 `#FFDDE0`, text 600 | bg 200 `#FFBFC5`, text 700 | bg 200 `#FFBFC5`, text 800 |
| Expired / error card | bg 50 `#FFF1F2` | bg 100 `#FFDDE0` | bg 200 `#FFBFC5` |
| Danger text link ("Remove") | 600 `#D13440` | 700 `#B52A35` | 800 `#8F1F29` |
| Error TextField border | 600 `#D13440` | 700 `#B52A35` (focus) | - |

---

## 6. Sky family (6 shades) — Cool: info

Sky logo ke halke cyan-blue (swoosh) se liya gaya hai. Ise info aur neutral actions (batches, stock movements) ke liye use karo, taaki wo brand blue se alag dikhein.

| Naam | Hex | Kab use karna hai |
|---|---|---|
| Sky 50 | `#EAF8FE` | Info card background, row press shade |
| Sky 100 | `#D0F0FC` | Icon tile (batches, movements), info pill background |
| Sky 200 | `#A4E2F9` | Icon tile press shade |
| **Sky 600** | **`#0B7FAE`** | Info icon aur text |
| Sky 700 | `#096890` | Info icon/text dabane par |
| Sky 800 | `#07516F` | Info icon/text hold par |

### Sky cheezon ka touch behavior

| Cheez | Normal | Press | Long press / hold |
|---|---|---|---|
| Icon tile (View batches, Stock movements) | bg 100 `#D0F0FC`, icon 600 | bg 200 `#A4E2F9`, icon 700 | bg 200 `#A4E2F9`, icon 800 |
| Info pill (tappable) | bg 100, text 600 | bg 200, text 700 | bg 200, text 800 |
| Info card | bg 50 `#EAF8FE` | bg 100 `#D0F0FC` | bg 200 `#A4E2F9` |
| Info text link | 600 `#0B7FAE` | 700 `#096890` | 800 `#07516F` |

> Jis row mein sky icon tile hai, poori row ka press shade **Blue 50 / 100** hi rahega (kyunki row card ka hissa hai). Sirf tile akela tappable ho to wo apne sky family ka press shade lega.

---

## 7. Neutral family (6 colors) — Text, surface, lines

Ye sab halke blue-tinted grays hain, taaki blue ke saath mel khayein.

| Naam | Hex | Kab use karna hai |
|---|---|---|
| White | `#FFFFFF` | Card, top bar, bottom bar, button ke upar ka text |
| Icon button fill | `#F3F7FD` | Chhote square buttons (bell, scan, back) ka normal background |
| Field fill | `#E8EFF8` | Info tile ka background (Category, Pack size wale boxes), input box |
| Ink | `#0E2240` | Heading aur main text |
| Muted | `#5B6F8C` | Subtitle, hint, placeholder, unselected icon aur label |
| Outline | `#DBE5F2` | Divider, card border, chip border |

### Neutral cheezon ka touch behavior

| Cheez | Normal | Press | Long press / hold |
|---|---|---|---|
| Text-only button (neutral, e.g. "Cancel") | Ink | Blue 700 text | Blue 800 text |
| Unselected bottom-nav item | transparent, icon Muted | bg Blue 50 | bg Blue 100 |
| Input box (focus) | border Outline | border Blue 500, 2dp | - |
| Divider / border | koi touch nahi | - | - |

Neutral cheezon ka touch shade blue family ka hi hai, kyunki poori app ka brand blue hai.

---

## 8. Logo only (3 colors)

Ye sirf logo, splash screen aur 1-2 illustrations ke liye hain. **Buttons, cards ya text mein mat lagao**, warna UI mein do-teen blues mix ho jayenge.

| Naam | Hex | Kahan |
|---|---|---|
| Logo bright blue | `#0888F8` | Logo ka shield aur capsule |
| Logo cyan | `#18B8F8` | Logo ki patti aur swoosh ka highlight |
| Logo lavender white | `#E8E8F8` | Logo ke capsule aur shield ke andar ka safed |

---

## 9. Baaki states (touch ke alawa)

| State | Kya dikhna chahiye |
|---|---|
| Selected | Blue 100 background + Blue 600 text/icon (ya status family ka 100 + 600) |
| Disabled button | bg Blue 300 `#86BAFB`, text white, koi press effect nahi |
| Disabled text | Muted `#5B6F8C` ka halka (50% opacity) |
| Focus (TextField) | Border Blue 500 `#1F82EC`, 2dp |
| Error (TextField) | Border Red 600 `#D13440`, neeche Red 600 ka message |
| Loading button | Normal shade rakho, andar white spinner |

---

## 10. Press animation ke niyam

- **Rang badalne ka time:** 100–120 ms (turant, par jhatke se nahi).
- **Scale:** press par 0.97 tak chhota ho (sirf buttons aur chhote cards par).
- **Long press:** hold shade + halka vibration (haptic). Phir koi action: menu, select mode, ya tooltip.
- **Press-and-hold:** jab tak ungli rakhi hai hold shade dikhe. Ungli hatate hi normal shade par wapas aa jaye.
- **Ripple (water wave) mat lagao.** Hamara apna shade badalna kaafi hai, aur dono ek saath dikhne se UI ganda lagta hai.
- **Row aur card** par scale mat lagao (sirf rang badle), kyunki list mein chhote jhatke ajeeb lagte hain.

---

## 11. Kahan kaun sa rang: ek nazar mein

| Screen ka hissa | Rang |
|---|---|
| Screen background | Blue 50 |
| Top bar / bottom bar | White |
| Page title | Ink |
| Subtitle | Muted |
| Primary button | Blue 600 (text white) |
| Secondary button | White + Outline border, text Ink |
| Search box | White + Outline border, placeholder Muted |
| Selected chip / nav | Blue 100 + Blue 600 |
| Product row | White, divider Outline |
| In stock / Available pill | Green 100 + Green 600 |
| Low stock / Near expiry pill | Amber 100 + Amber 600 |
| Out of stock / Expired pill | Red 100 + Red 600 |
| Batches / Movements icon tile | Sky 100 + Sky 600 |
| Detail info tile | Field fill `#E8EFF8` |
| Hero / header gradient | Blue 800 → Blue 600 → Blue 400 |

---

## 12. Contrast (padhne layak hone ki jaanch)

Ye numbers maine hex values se calculate kiye hain (WCAG formula). Normal text ke liye 4.5 : 1 chahiye.

| Jodi | Contrast | Natija |
|---|---|---|
| White text on Blue 600 | 4.86 : 1 | Theek |
| White text on Blue 700 | 6.78 : 1 | Achha |
| White text on Blue 800 | 9.39 : 1 | Bahut achha |
| Blue 600 text on White | 4.86 : 1 | Theek |
| Blue 600 text on Blue 50 | 4.43 : 1 | Sirf bold ya bade text ke liye |
| Ink on White | 15.9 : 1 | Bahut achha |
| Muted on White | 5.13 : 1 | Theek |
| Muted on Blue 50 | 4.67 : 1 | Theek |
| Green 600 text on Green 100 | ~4.1 : 1 | Sirf bold/chhote labels ke liye |
| Amber 600 text on Amber 100 | 3.27 : 1 | Kamzor, 700 shade lo |
| Red 600 text on Red 100 | 3.9 : 1 | Kamzor, 700 shade lo |
| Sky 600 text on Sky 100 | 3.77 : 1 | Kamzor, 700 shade lo |

**Pill ke chhote text ke liye:** Amber, Red aur Sky pills mein text 600 ki jagah **700** shade (Amber 700 4.69, Red 700 4.98, Sky 700 5.17) rakho aur text **bold** karo. Isse saaf padha jayega. Touch tables mein pill ke press/hold shade isi hisaab se 700 aur 800 hi hain, isliye koi aur badlav nahi chahiye.

---

## 13. Yaad rakhne ke 6 niyam

1. **Ek brand blue:** `#0A6EE0`. Uske alawa koi doosra blue UI ke buttons mein nahi.
2. **Rang ka matlab fix rakho:** blue = action/brand, green = theek hai, amber = dhyan do, red = khatra, sky = jaankari.
3. **Touch par family wahi:** blue ka press blue hi rahe, warm ka press warm hi rahe.
4. **Halke background → gehra karo (50 → 100 → 200). Gehre background → aur gehra karo (600 → 700 → 800).**
5. **Press aur hold ke shade alag dikhein**, aur ungli hatate hi normal par wapas aao.
6. **Green ko brand ki tarah mat lagao.** Wo sirf success ke liye reserved hai.