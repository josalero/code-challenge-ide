use challenge::bubble_sort;

#[test]
fn public_bubblesort_3_1_2_should_return_1_2_3() {
    assert_eq!(bubble_sort(&[3, 1, 2]), vec![1, 2, 3]);
}
