# How to Contribute to Default Dark Mode: Expansion

Thanks for wanting to help improve **Default Dark Mode: Expansion**! Follow these simple rules to keep everything consistent and awesome.

---

## Important Rules

1. **Stick to the Style**  
   - This pack keeps the **vanilla Minecraft look** with a dark mode twist.  
   - Make sure your changes fit this style.

2. **When to Use Automation vs. Manual Editing**  
   - **Manual (Krita):** For mods that mimic vanilla’s GUI style (e.g., JEI, Storage Drawers, Baubles). These need careful hand‑tuning to stay faithful.  
   - **Automated (Python Script):** For large mods with many custom GUIs (e.g., Blood Magic, Thaumcraft, Ender IO, Witchery). Use the provided script below to batch‑darken textures consistently.  
   - ⚠️ **Important:** Automation is **not good for fine elements** like bars, buttons, icons, and widgets. These often lose readability or contrast when batch‑darkened. Always review automated results and fix/revert these elements manually in Krita.  
   - ❌ **Never use online quick tools** like [PineTools](https://pinetools.com/darken-image) for vanilla‑style GUIs — it fails to preserve the right look.

3. **Use Krita for Precision**  
   - [Krita](https://krita.org/) is still the go‑to for detailed edits, touch‑ups, and polishing after automation.

4. **Organize Files Properly**  
   - Place new textures in the correct folders. Match the current structure and file naming.  
   - If you use the Python script, it will output to a `darkmode-output` folder. Copy results into the right `assets/modid/textures/gui/` path.

5. **Test Your Changes**  
   - Load the pack in Minecraft to make sure your changes work and look good.  
   - Check both light and dark GUIs for consistency.

---

## Automation Script

I now provide a Python script to help contributors batch‑darken GUI textures. This saves time and ensures consistency.

### Requirements
- [Python 3.x](https://www.python.org/downloads/)
- [Pillow](https://pypi.org/project/Pillow/) library (`pip install pillow`)

### Script

Save this as `automate-dark-mode-textures.py`:

```python
from PIL import Image
import os

# Input and output directories
input_dir = r"C:\path\to\your\new-assets"
output_dir = os.path.join(input_dir, "darkmode-output")
os.makedirs(output_dir, exist_ok=True)

# Darkening factor (0.4 = 40% brightness)
FACTOR = 0.4

# Optional grayscale mode (OFF by default)
GRAYSCALE = False

def darken_color(color, factor=FACTOR):
    r, g, b, *a = color
    r = int(r * factor)
    g = int(g * factor)
    b = int(b * factor)
    if a:  # preserve alpha channel if present
        return (r, g, b, a[0])
    return (r, g, b)

def to_grayscale(color):
    r, g, b, *a = color
    gray = int(0.299*r + 0.587*g + 0.114*b)
    if a:
        return (gray, gray, gray, a[0])
    return (gray, gray, gray)

for root, _, files in os.walk(input_dir):
    for file in files:
        if file.endswith(".png"):
            in_path = os.path.join(root, file)
            rel_path = os.path.relpath(in_path, input_dir)
            out_path = os.path.join(output_dir, rel_path)

            os.makedirs(os.path.dirname(out_path), exist_ok=True)

            img = Image.open(in_path).convert("RGBA")
            pixels = img.getdata()

            # Apply darkening
            new_pixels = [darken_color(p) for p in pixels]

            # Optional grayscale step
            if GRAYSCALE:
                new_pixels = [to_grayscale(p) for p in new_pixels]

            img.putdata(new_pixels)
            img.save(out_path)

print("✅ Dark mode textures generated in:", output_dir)
