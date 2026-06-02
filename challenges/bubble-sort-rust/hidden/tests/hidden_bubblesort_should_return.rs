use challenge::bubble_sort;

#[test]
fn hidden_bubblesort_should_return() {
    assert_eq!(bubble_sort(&[]), vec![]);
}
