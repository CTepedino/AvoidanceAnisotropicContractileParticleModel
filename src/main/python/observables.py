import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import math
import numpy as np
from collections import defaultdict
import os

def analyze_multiple_qin_interval_averaged(file_prefixes, i_range=range(1, 11), dt=1.0, t_start=10.0, t_end=40.0, output_plot="vx_vs_qin.png"):
    qin_values = []
    mean_vx_all = []
    std_errors = []

    for prefix in file_prefixes:
        qin_str = prefix.split("_")[-2]  # e.g., from 'output_Qin_8_i_' -> '8'
        try:
            qin = float(qin_str)
        except ValueError:
            print(f"Could not extract Qin from prefix: {prefix}")
            continue

        qin_values.append(qin)

        vx_values = []

        for i in i_range:
            file_path = f"{prefix}{i}.txt"
            if not os.path.exists(file_path):
                print(f"Missing file: {file_path}, skipping...")
                continue

            mean_vx, _ = get_absolute_vx_mean_displacement_based(
                file_path, dt=dt, t_start=t_start, t_end=t_end
            )
            vx_values.append(mean_vx)

        if len(vx_values) == 0:
            print(f"No data for Qin = {qin}")
            mean_vx_all.append(0.0)
            std_errors.append(0.0)
            continue

        vx_array = np.array(vx_values)
        mean_vx = np.mean(vx_array)
        std_error = np.std(vx_array, ddof=1) / np.sqrt(len(vx_array))

        mean_vx_all.append(mean_vx)
        std_errors.append(std_error)

    # Ordenar por Qin por si acaso
    qin_values, mean_vx_all, std_errors = zip(*sorted(zip(qin_values, mean_vx_all, std_errors)))

    # Plot
    plt.figure(figsize=(10,6))
    plt.errorbar(qin_values, mean_vx_all, yerr=std_errors, fmt='o-', capsize=5, ecolor="black")
    plt.xlabel("Qin (1/s)", fontsize=20)
    plt.ylabel("<|vx|> (m/s)", fontsize=20)
    plt.xticks(fontsize=20)
    plt.yticks(fontsize=20)
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(output_plot)
    print(f"Saved plot as {output_plot}")

    # Report
    for q, m, e in zip(qin_values, mean_vx_all, std_errors):
        print(f"Qin = {q:.3f} -> <|vx|> = {m:.4f} ± {e:.4f}")


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

def plot_avg_final_time_vs_qin(file_prefixes, i_range=range(1, 11), output_plot="tmax_vs_qin.png"):
    qin_values = []
    t_max_means = []
    t_max_errors = []

    for prefix in file_prefixes:
        qin_str = prefix.split("_")[-2]  # Extrae el número de Qin del prefijo
        try:
            qin = float(qin_str)
        except ValueError:
            print(f"Could not extract Qin from prefix: {prefix}")
            continue

        t_max_list = []

        for i in i_range:
            file_path = f"{prefix}{i}.txt"
            if not os.path.exists(file_path):
                print(f"Missing file: {file_path}, skipping...")
                continue

            t_max = get_last_simulation_time(file_path)
            t_max_list.append(t_max)

        if len(t_max_list) == 0:
            print(f"No data for Qin = {qin}")
            continue

        t_max_array = np.array(t_max_list)
        t_max_mean = np.mean(t_max_array)
        t_max_error = np.std(t_max_array, ddof=1) / np.sqrt(len(t_max_array))

        qin_values.append(qin)
        t_max_means.append(t_max_mean)
        t_max_errors.append(t_max_error)

    # Ordenar por Qin
    qin_values, t_max_means, t_max_errors = zip(*sorted(zip(qin_values, t_max_means, t_max_errors)))

    # Plot
    plt.figure(figsize=(10,6))
    plt.errorbar(qin_values, t_max_means, yerr=t_max_errors, fmt='o-', capsize=5, ecolor="black")
    plt.xlabel("Qin (1/s)", fontsize=20)
    plt.ylabel("<tf> (s)", fontsize=20)
    plt.xticks(fontsize=20)
    plt.yticks(fontsize=20)
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(output_plot)
    print(f"Saved plot to {output_plot}")

    # Reporte en consola
    for q, m, e in zip(qin_values, t_max_means, t_max_errors):
        print(f"Qin = {q:.3f} -> t_max = {m:.4f} ± {e:.4f} s")


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

def plot_vx_evolution(vx_per_t, qin):
    times = sorted(vx_per_t.keys())
    mean_vxs = [float(vx_per_t[t][0]) for t in times]
    stderr_vxs = [float(vx_per_t[t][1]) for t in times]
    
    plt.figure(figsize=(20, 8))
    plt.errorbar(times, mean_vxs, yerr=stderr_vxs, 
                 fmt='o-', 
                 capsize=2,           # Smaller cap size
                 ecolor='gray',       # Gray error bars instead of black
                 alpha=0.6,           # Semi-transparent error bars
                 markersize=3,        # Smaller markers
                 linewidth=2,         # Thicker main line
                 elinewidth=0.8,      # Thinner error bar lines
                 capthick=0.8)        # Thinner caps
    
    plt.xlabel('Tiempo (s)', fontsize=20)
    plt.ylabel('<|vx|> (m/s)', fontsize=20)
    plt.xticks(fontsize=18)
    plt.yticks(fontsize=18)
    plt.grid(True, linestyle='--', alpha=0.3)  # Lighter grid
    plt.tight_layout()
    plt.savefig(f"mean_vx_vs_time_for_qin_{qin}.png", dpi=300, bbox_inches='tight')

def get_avg_mean_vx_per_timestep_for_prefix(prefix, i_range=range(1, 11), dt=1.0, t_start=0.0, t_end=np.inf, epsilon=1e-6):
    # Acumulador global de velocidades por t1
    vx_by_time_all = defaultdict(list)

    for i in i_range:
        file_path = f"{prefix}{i}.txt"
        if not os.path.exists(file_path):
            print(f"Archivo no encontrado: {file_path}, se omite.")
            continue

        particle_data = defaultdict(list)

        # Leer datos por archivo
        with open(file_path, 'r') as f:
            for line in f:
                parts = line.strip().split()
                if len(parts) != 7:
                    continue
                t, pid, x, y, _, _, _ = map(float, parts)
                pid = int(pid)
                particle_data[pid].append((t, x))

        # Calcular |vx| por partícula en este archivo
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
                    vx_by_time_all[t1].append(abs_vx)

    result = {}
    for t1 in sorted(vx_by_time_all.keys()):
        vxs = vx_by_time_all[t1]
        mean_vx = np.mean(vxs)
        stderr_vx = np.std(vxs, ddof=1) / np.sqrt(len(vxs)) if len(vxs) > 1 else 0.0
        result[t1] = (mean_vx, stderr_vx)

    return result


if __name__ == '__main__':
    file_prefixes = [
        "output_Qin_1.00_",
        "output_Qin_2.00_",
        "output_Qin_3.00_",
        "output_Qin_4.00_",
        "output_Qin_5.00_",
        "output_Qin_6.00_",
        "output_Qin_7.00_",
        "output_Qin_8.00_",
        "output_Qin_9.00_",
        "output_Qin_10.00_",
    ]

    #uncomment this

    #analyze_multiple_qin_interval_averaged(file_prefixes)
    #plot_avg_final_time_vs_qin(file_prefixes, output_plot="tmax_vs_qin.png")

    #for file_prefix in file_prefixes:
    #    qin = float(file_prefix.split("_")[-2])
    #    vx_per_t = get_avg_mean_vx_per_timestep_for_prefix(file_prefix, dt=1.0)
    #    plot_vx_evolution(vx_per_t, qin)