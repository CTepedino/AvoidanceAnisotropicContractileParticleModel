import math

import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation

# Cargar datos
df = pd.read_csv("out.txt", sep=r"\s+", engine="python", header=None,
                 names=["time", "id", "x", "y", "vx", "vy", "radius"])


times = sorted(df["time"].unique())
particle_ids = sorted(df["id"].unique())
frames = {t: df[df["time"] == t] for t in times}

corridor_length = 16.0
corridor_width = 3.6

fig, ax = plt.subplots(figsize=(10, 2.5))
ax.set_xlim(0, corridor_length)
ax.set_ylim(0, corridor_width)

ax.set_yticks([])
ax.set_xticks([])

particles_phys = {}      # Círculo físico (radio fijo)
particles_interact = {}  # Círculo de interacción (radio dinámico)
colors_por_id = {}
direcciones = {}
completadas = set()

# Crear círculos físicos e interacción
for pid in particle_ids:
    # Cuerpo físico (pequeño, relleno)
    body = plt.Circle((0, 0), 0.1, color="gray", alpha=0.8)
    body.set_visible(False)
    ax.add_patch(body)
    particles_phys[pid] = body

    # Radio de interacción (grande, borde)
    halo = plt.Circle((0, 0), 0.1, edgecolor="gray", fill=False, linestyle="--", alpha=0.4)
    halo.set_visible(False)
    ax.add_patch(halo)
    particles_interact[pid] = halo

def asignar_color(vx):
    if vx > 0:
        return "blue"
    elif vx < 0:
        return "red"
    else:
        return "black"

def update(frame_idx):
    t = times[frame_idx]
    data = frames[t]

    row_line = 0

    for _, row in data.iterrows():
        row_line +=1

        pid = row["id"]
        if pid in completadas:
            continue

        if math.isnan(pid):
            print(f"nan in line {row_line}")

        x, y = row["x"], row["y"]
        vx = row["vx"]
        r_interact = row["radius"]

        # Registrar dirección
        if pid not in direcciones:
            direcciones[pid] = 1 if vx > 0 else -1
        else:
            sentido = direcciones[pid]
            if (sentido == 1 and x >= 15.75) or (sentido == -1 and x <= 0.25):
                particles_interact[pid].set_visible(False)
                particles_phys[pid].set_visible(False)
                completadas.add(pid)
                continue

        sentido = direcciones[pid]
        if (sentido == 1 and x >= corridor_length) or (sentido == -1 and x <= 0):
            particles_phys[pid].set_visible(False)
            particles_interact[pid].set_visible(False)
            completadas.add(pid)
            continue

        if pid not in colors_por_id:
            colors_por_id[pid] = asignar_color(vx)


        body = particles_phys[pid]
        body.center = (x, y)
        body.set_color(colors_por_id[pid])
        body.set_visible(True)

        # Radio de interacción
        halo = particles_interact[pid]
        halo.center = (x, y)
        halo.set_radius(r_interact)
        halo.set_edgecolor(colors_por_id[pid])
        halo.set_visible(True)

    return list(particles_phys.values()) + list(particles_interact.values())

ani = FuncAnimation(fig, update, frames=len(times), interval=30, blit=True)
#
# plt.tight_layout()
# plt.show()

ani.save("animation.mp4", fps=30)
