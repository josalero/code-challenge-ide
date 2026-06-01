use challenge::plus_one;

#[test]
fn hidden_plusone_9_9_9_should_return_1_0_0_0() {
    assert_eq!(plus_one(&[9, 9, 9]), vec![1, 0, 0, 0]);
}
