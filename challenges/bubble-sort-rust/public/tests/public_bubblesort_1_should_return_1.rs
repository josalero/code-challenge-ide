use challenge::bubble_sort;

#[test]
fn public_bubblesort_1_should_return_1() {
    assert_eq!(bubble_sort(&[1]), vec![1]);
}
