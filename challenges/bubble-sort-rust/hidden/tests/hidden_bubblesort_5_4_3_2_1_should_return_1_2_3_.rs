use challenge::bubble_sort;

#[test]
fn hidden_bubblesort_5_4_3_2_1_should_return_1_2_3_() {
    assert_eq!(bubble_sort(&[5, 4, 3, 2, 1]), vec![1, 2, 3, 4, 5]);
}
