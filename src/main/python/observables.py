import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import math
import numpy as np
from collections import defaultdict
import os

def get_absolute_vx_mean_displacement_based(file_path, dt=1.0, t_start=0.0, t_end=np.inf, epsilon=1e-6):
    particle_data = defaultdict(list)

    with open(file_path, 'r') as f:
        for line in f:
            parts = line.strip().split()
            if len(parts) != 7:
                continue

            t, pid, x, y, _, _, _ = map(float, parts)
            pid = int(pid)
            particle_data[pid].append((t, x))
    abs_vxs = []

    for pid, states in particle_data.items():
        times_x = {round(t, 6): x for t, x in states}
        sorted_times = sorted(times_x.keys())

        for t0 in sorted_times:
            if not (t_start <= t0 <= t_end - dt):
                continue
            t1 = round(t0 + dt, 6)
            if t1 in times_x:
                dx = times_x[t1] - times_x[t0]
                abs_vx = abs(dx / dt)
                abs_vxs.append(abs_vx)

    if abs_vxs:
        mean_val = np.mean(abs_vxs)
        std_err = np.std(abs_vxs) / np.sqrt(len(abs_vxs))
    else:
        mean_val = 0.0
        std_err = 0.0

    return mean_val, std_err

def analyze_multiple_qin_interval(file_paths, dt=1.0, t_start=0.0, t_end=np.inf, output_plot="vx_vs_qin.png"):
    qin_values = []
    mean_vx_all = []
    std_errors = []

    for file_path in file_paths:
        qin_str = os.path.splitext(os.path.basename(file_path))[0].split("_")[-1]
        try:
            qin = float(qin_str)
        except ValueError:
            print(f"Could not extract Qin from filename: {file_path}")
            continue

        qin_values.append(qin)

        mean_vx, std_err = get_absolute_vx_mean_displacement_based(
            file_path, dt=dt, t_start=t_start, t_end=t_end
        )

        mean_vx_all.append(mean_vx)
        std_errors.append(std_err)

    # Plot
    plt.errorbar(qin_values, mean_vx_all, yerr=std_errors, fmt='o-', capsize=5)
    plt.xlabel("Qin")
    plt.ylabel("<|vx|> promedio (m/s)")
    plt.title(f"<|vx|> promedio vs Qin\nIntervalo t ∈ [{t_start}, {t_end}] s")
    plt.grid(True)
    plt.savefig(output_plot)
    print(f"Saved plot as {output_plot}")

    # Report
    for q, m, e in zip(qin_values, mean_vx_all, std_errors):
        print(f"Qin = {q:.3f} -> <|vx|> = {m:.4f} ± {e:.4f}")

def get_last_simulation_time(file_path):
    """
    Returns the maximum t value found in the file.
    """
    max_t = float('-inf')
    with open(file_path, 'r') as f:
        for line in f:
            parts = line.strip().split()
            if len(parts) >= 1:
                try:
                    t = float(parts[0])
                    if t > max_t:
                        max_t = t
                except ValueError:
                    continue
    return max_t

def extract_qin_from_filename(file_name):
    """
    Extract Qin value from filename like output_Qin_1.5.txt
    """
    try:
        base = os.path.splitext(os.path.basename(file_name))[0]
        return float(base.split("_")[-1])
    except Exception:
        return None

def plot_final_time_vs_qin(file_paths, output_plot="tmax_vs_qin.png"):
    qin_values = []
    t_max_values = []

    for path in file_paths:
        qin = extract_qin_from_filename(path)
        if qin is None:
            print(f"Skipping file: {path} (could not extract Qin)")
            continue

        t_max = get_last_simulation_time(path)
        qin_values.append(qin)
        t_max_values.append(t_max)

    # Sort by Qin
    qin_values, t_max_values = zip(*sorted(zip(qin_values, t_max_values)))

    # Plot
    plt.figure()
    plt.plot(qin_values, t_max_values, 'o-', label="t max por simulación")
    plt.xlabel("Qin")
    plt.ylabel("Último t en la simulación (s)")
    plt.title("Duración total de la simulación vs Qin")
    plt.grid(True)
    plt.legend()
    plt.savefig(output_plot)
    print(f"Saved plot to {output_plot}")


def get_mean_vx_per_timestep(file_path, dt=1.0, t_start=0.0, t_end=np.inf, epsilon=1e-6):
    particle_data = defaultdict(list)

    # Leer datos
    with open(file_path, 'r') as f:
        for line in f:
            parts = line.strip().split()
            if len(parts) != 7:
                continue
            t, pid, x, y, _, _, _ = map(float, parts)
            pid = int(pid)
            particle_data[pid].append((t, x))

    # Diccionario para acumular |vx| por tiempo t1
    vx_by_time = defaultdict(list)

    for pid, states in particle_data.items():
        times_x = {round(t, 6): x for t, x in states}
        sorted_times = sorted(times_x.keys())

        for t0 in sorted_times:
            if not (t_start <= t0 <= t_end - dt):
                continue
            t1 = round(t0 + dt, 6)
            if t1 in times_x:
                dx = times_x[t1] - times_x[t0]
                abs_vx = abs(dx / dt)
                vx_by_time[t1].append(abs_vx)

    # Calcular media y error estándar por instante de tiempo t1
    result = {}
    for t1, vxs in sorted(vx_by_time.items()):
        mean_vx = np.mean(vxs)
        stderr_vx = np.std(vxs) / np.sqrt(len(vxs)) if len(vxs) > 1 else 0.0
        result[t1] = (mean_vx, stderr_vx)

    return result

import matplotlib.pyplot as plt

# Suponiendo que ya ejecutaste:
# vx_per_t = get_mean_vx_per_timestep('data.txt', dt=1.0)

def plot_vx_evolution(vx_per_t, qin):
    times = sorted(vx_per_t.keys())
    mean_vxs = [vx_per_t[t][0] for t in times]
    stderr_vxs = [vx_per_t[t][1] for t in times]

    plt.figure(figsize=(10, 5))
    plt.errorbar(times, mean_vxs, yerr=stderr_vxs, fmt='o-', capsize=4, ecolor='gray', label='⟨|vx|⟩ ± stderr')

    plt.title('Evolución temporal de la velocidad media |vx|')
    plt.xlabel('Tiempo (s)')
    plt.ylabel('Velocidad media |vx|')
    plt.grid(True, linestyle='--', alpha=0.5)
    plt.legend()
    plt.tight_layout()
    plt.savefig(f"mean_vx_vs_time_for_qin_{qin}.png")


if __name__ == '__main__':
    file_paths = [
        "output_Qin_1.00.txt",
        "output_Qin_2.00.txt",
        "output_Qin_3.00.txt",
        "output_Qin_4.00.txt",
        "output_Qin_5.00.txt",
        "output_Qin_6.00.txt",
        "output_Qin_7.00.txt",
        "output_Qin_8.00.txt",
        "output_Qin_9.00.txt",
        "output_Qin_10.00.txt",
    ]

    analyze_multiple_qin_interval(file_paths, dt=1, t_start=10.0, t_end=40.0)
    plot_final_time_vs_qin(file_paths, output_plot="tmax_vs_qin.png")
    for file in file_paths:
        result = get_mean_vx_per_timestep(file)
        plot_vx_evolution(result, file.split('.')[0][-1])