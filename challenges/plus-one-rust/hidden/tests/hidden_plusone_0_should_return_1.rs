use challenge::plus_one;

#[test]
fn hidden_plusone_0_should_return_1() {
    assert_eq!(plus_one(&[0]), vec![1]);
}
