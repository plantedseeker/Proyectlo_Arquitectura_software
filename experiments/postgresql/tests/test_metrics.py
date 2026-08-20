import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1]))

from metrics import percentile, summarize


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


if __name__ == "__main__":
    unittest.main()
