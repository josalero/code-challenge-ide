use challenge::plus_one;

#[test]
fn public_plusone_1_2_3_should_return_1_2_4() {
    assert_eq!(plus_one(&[1, 2, 3]), vec![1, 2, 4]);
}
