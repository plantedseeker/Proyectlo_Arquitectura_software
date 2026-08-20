import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1]))

from metrics import percentile, summarize, summarize_valid_runs


class MetricsTest(unittest.TestCase):
    def test_nearest_rank_p95_is_deterministic(self):
        self.assertEqual(95, percentile(range(1, 101), 95))

    def test_summary_uses_all_samples(self):
        result = summarize([10.0, 20.0, 30.0, 40.0])
        self.assertEqual(4, result["samples"])
        self.assertEqual(25.0, result["mean_ms"])
        self.assertEqual(40.0, result["p95_ms"])

    def test_empty_input_is_rejected(self):
        with self.assertRaises(ValueError):
            summarize([])

    def test_valid_run_summary_registers_median_p95(self):
        result = summarize_valid_runs(
            [
                {"run": 2, "p95_ms": 24.763},
                {"run": 3, "p95_ms": 22.430},
                {"run": 4, "p95_ms": 41.364},
            ]
        )
        self.assertEqual([2, 3, 4], result["valid_run_numbers"])
        self.assertEqual(24.763, result["p95_median_ms"])
        self.assertEqual(22.430, result["p95_min_ms"])
        self.assertEqual(41.364, result["p95_max_ms"])

    def test_valid_run_summary_rejects_empty_input(self):
        with self.assertRaises(ValueError):
            summarize_valid_runs([])


if __name__ == "__main__":
    unittest.main()
