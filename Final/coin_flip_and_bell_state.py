
from qiskit import QuantumCircuit, transpile
from qiskit_aer import AerSimulator
import matplotlib.pyplot as plt


def circuit_to_ascii_diagram(circuit):
    try:
        return str(circuit.draw(output="text", use_unicode=False, fold=-1))
    except TypeError:
        return str(circuit.draw(output="text", fold=-1))


def run_on_simulator(circuit, *, shots=2048, seed=123):
    backend = AerSimulator(seed_simulator=seed)
    compiled = transpile(circuit, backend)
    job = backend.run(compiled, shots=shots)
    result = job.result()
    return dict(result.get_counts())

# Part 1 — Quantum Coin Flip
def build_coin_flip_circuit():
    qc = QuantumCircuit(1, 1, name="coin_flip")
    qc.h(0)
    qc.measure(0, 0)
    return qc


coin = build_coin_flip_circuit()
print("Coin flip circuit:")
print(circuit_to_ascii_diagram(coin))

shots = 2048
seed = 123
coin_counts = run_on_simulator(coin, shots=shots, seed=seed)
print("Coin flip counts:", coin_counts)

keys = sorted(coin_counts.keys())
values = [coin_counts[k] for k in keys]
plt.figure(figsize=(4, 3))
plt.bar(keys, values)
plt.title("Quantum Coin Flip counts")
plt.xlabel("Measured value")
plt.ylabel("Counts")

# Part 2 — Bell state
def build_bell_state_circuit():
    qc = QuantumCircuit(2, 2, name="bell_state")
    qc.h(0)
    qc.cx(0, 1)
    qc.measure(0, 0)
    qc.measure(1, 1)
    return qc


bell = build_bell_state_circuit()
print("\nBell state circuit:")
print(circuit_to_ascii_diagram(bell))

bell_counts = run_on_simulator(bell, shots=shots, seed=seed)
print("Bell state counts:", bell_counts)

keys = sorted(bell_counts.keys())
values = [bell_counts[k] for k in keys]
plt.figure(figsize=(4, 3))
plt.bar(keys, values)
plt.title("Bell State measurement counts")
plt.xlabel("Measured bitstring")
plt.ylabel("Counts")

plt.show()
